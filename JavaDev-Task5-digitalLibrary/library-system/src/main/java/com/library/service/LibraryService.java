package com.library.service;

import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.repository.IssueRecordRepository;
import com.library.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final ReservationRepository reservationRepository;

    @Value("${library.issue.loan-days:14}")
    private int loanDays;

    @Value("${library.fine.per-day:5.0}")
    private double finePerDay;

    public LibraryService(BookRepository bookRepository,
                           IssueRecordRepository issueRecordRepository,
                           ReservationRepository reservationRepository) {
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.reservationRepository = reservationRepository;
    }

    /** Issue a book to a member. Honors any READY reservation held for someone else. */
    @Transactional
    public IssueRecord issueBook(Book book, Member member) {
        // If another member has a READY reservation on this book, this member cannot jump the queue.
        List<Reservation> readyForOthers = reservationRepository
                .findByBookAndStatusOrderByReservedAtAsc(book, Reservation.Status.READY);
        boolean heldForSomeoneElse = readyForOthers.stream()
                .anyMatch(r -> !r.getMember().getId().equals(member.getId()));
        if (heldForSomeoneElse) {
            throw new IllegalStateException("This copy is being held for another member's reservation.");
        }

        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("No copies of this book are currently available.");
        }

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        IssueRecord record = new IssueRecord();
        record.setBook(book);
        record.setMember(member);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(loanDays));
        record.setStatus(IssueRecord.Status.ISSUED);
        IssueRecord saved = issueRecordRepository.save(record);

        // If this member had a READY/PENDING reservation for this book, mark it fulfilled.
        Optional<Reservation> own = reservationRepository.findFirstByBookAndMemberAndStatusIn(
                book, member, List.of(Reservation.Status.READY, Reservation.Status.PENDING));
        own.ifPresent(r -> {
            r.setStatus(Reservation.Status.FULFILLED);
            reservationRepository.save(r);
        });

        return saved;
    }

    /** Return a book, auto-calculating any overdue fine, then release the copy (honoring reservations). */
    @Transactional
    public IssueRecord returnBook(IssueRecord record) {
        if (record.getStatus() == IssueRecord.Status.RETURNED) {
            throw new IllegalStateException("This book has already been returned.");
        }
        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setStatus(IssueRecord.Status.RETURNED);

        long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
        if (overdueDays > 0) {
            record.setFineAmount(overdueDays * finePerDay);
        }
        issueRecordRepository.save(record);

        Book book = record.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        bookRepository.save(book);

        // If someone is waiting (PENDING), earmark this freed copy for them.
        List<Reservation> pending = reservationRepository
                .findByBookAndStatusOrderByReservedAtAsc(book, Reservation.Status.PENDING);
        if (!pending.isEmpty()) {
            Reservation next = pending.get(0);
            next.setStatus(Reservation.Status.READY);
            reservationRepository.save(next);
        }

        return record;
    }

    /** Reserve a book that is currently fully issued out (advance booking). */
    @Transactional
    public Reservation reserveBook(Book book, Member member) {
        if (book.getAvailableQuantity() > 0) {
            throw new IllegalStateException("This book is currently available — you can issue it directly instead of reserving it.");
        }
        boolean alreadyActive = reservationRepository
                .findFirstByBookAndMemberAndStatusIn(book, member,
                        List.of(Reservation.Status.PENDING, Reservation.Status.READY))
                .isPresent();
        if (alreadyActive) {
            throw new IllegalStateException("You already have an active reservation for this book.");
        }

        Reservation r = new Reservation();
        r.setBook(book);
        r.setMember(member);
        r.setStatus(Reservation.Status.PENDING);
        return reservationRepository.save(r);
    }

    @Transactional
    public void cancelReservation(Reservation r) {
        r.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(r);
    }

    public Optional<Reservation> findReservation(Long id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public void markFinePaid(IssueRecord record) {
        record.setFinePaid(true);
        issueRecordRepository.save(record);
    }

    public List<IssueRecord> allIssued() {
        return issueRecordRepository.findByStatus(IssueRecord.Status.ISSUED);
    }

    public List<IssueRecord> historyFor(Member member) {
        return issueRecordRepository.findByMemberOrderByIssueDateDesc(member);
    }

    public List<Reservation> reservationsFor(Member member) {
        return reservationRepository.findByMemberOrderByReservedAtDesc(member);
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAllByOrderByReservedAtDesc();
    }

    public List<IssueRecord> unpaidFines() {
        return issueRecordRepository.findByFinePaidFalseAndFineAmountGreaterThan(0.0);
    }

    public double finePerDay() {
        return finePerDay;
    }

    public int loanDays() {
        return loanDays;
    }
}
