package com.library.repository;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMemberOrderByReservedAtDesc(Member member);
    List<Reservation> findByBookAndStatusOrderByReservedAtAsc(Book book, Reservation.Status status);
    Optional<Reservation> findFirstByBookAndMemberAndStatusIn(Book book, Member member, List<Reservation.Status> statuses);
    List<Reservation> findAllByOrderByReservedAtDesc();
}
