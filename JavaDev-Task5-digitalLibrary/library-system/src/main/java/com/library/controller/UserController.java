package com.library.controller;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.service.BookService;
import com.library.service.LibraryService;
import com.library.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final BookService bookService;
    private final LibraryService libraryService;
    private final MemberService memberService;

    public UserController(BookService bookService, LibraryService libraryService, MemberService memberService) {
        this.bookService = bookService;
        this.libraryService = libraryService;
        this.memberService = memberService;
    }

    private Member currentMember(Authentication auth) {
        return memberService.getByEmail(auth.getName());
    }

    @GetMapping("/catalogue")
    public String catalogue(@RequestParam(required = false) String category, Model model) {
        List<Book> books = (category == null || category.isBlank())
                ? bookService.findAll()
                : bookService.findByCategory(category);
        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("selectedCategory", category);
        return "user/catalogue";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        List<Book> results = (q == null || q.isBlank()) ? List.of() : bookService.search(q);
        model.addAttribute("books", results);
        model.addAttribute("query", q);
        return "user/search";
    }

    @GetMapping("/my-books")
    public String myBooks(Authentication auth, Model model) {
        Member member = currentMember(auth);
        List<IssueRecord> history = libraryService.historyFor(member);
        List<Reservation> reservations = libraryService.reservationsFor(member);

        Map<Long, Long> overdueDaysByRecord = history.stream()
                .filter(r -> r.getStatus() == IssueRecord.Status.ISSUED && r.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toMap(IssueRecord::getId,
                        r -> ChronoUnit.DAYS.between(r.getDueDate(), LocalDate.now())));

        model.addAttribute("history", history);
        model.addAttribute("reservations", reservations);
        model.addAttribute("overdueDays", overdueDaysByRecord);
        model.addAttribute("finePerDay", libraryService.finePerDay());
        return "user/my-books";
    }

    @PostMapping("/issue/{bookId}")
    public String issue(@PathVariable Long bookId, Authentication auth,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Member member = currentMember(auth);
        Book book = bookService.findById(bookId).orElseThrow();
        try {
            libraryService.issueBook(book, member);
            redirectAttributes.addFlashAttribute("success",
                    "\"" + book.getTitle() + "\" issued Due back on " +
                            LocalDate.now().plusDays(libraryService.loanDays()) + ".");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/catalogue";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserve(@PathVariable Long bookId, Authentication auth,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Member member = currentMember(auth);
        Book book = bookService.findById(bookId).orElseThrow();
        try {
            libraryService.reserveBook(book, member);
            redirectAttributes.addFlashAttribute("success",
                    "\"" + book.getTitle() + "\" reserved — you'll be able to issue it as soon as a copy is returned.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/catalogue";
    }

    @PostMapping("/return/{issueId}")
    public String returnBook(@PathVariable Long issueId,
                              org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        IssueRecord record = libraryService.allIssued().stream()
                .filter(r -> r.getId().equals(issueId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found"));
        libraryService.returnBook(record);
        String msg = record.getFineAmount() > 0
                ? "Book returned. A fine of \u20B9" + record.getFineAmount() + " applies for late return."
                : "Book returned on time — thanks!";
        redirectAttributes.addFlashAttribute("success", msg);
        return "redirect:/my-books";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, Authentication auth,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Member member = currentMember(auth);
        libraryService.findReservation(id)
                .filter(r -> r.getMember().getId().equals(member.getId()))
                .ifPresentOrElse(
                        r -> {
                            libraryService.cancelReservation(r);
                            redirectAttributes.addFlashAttribute("success", "Reservation cancelled.");
                        },
                        () -> redirectAttributes.addFlashAttribute("error", "Reservation not found.")
                );
        return "redirect:/my-books";
    }
}
