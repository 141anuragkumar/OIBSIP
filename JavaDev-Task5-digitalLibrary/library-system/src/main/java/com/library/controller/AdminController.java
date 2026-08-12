package com.library.controller;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.service.BookService;
import com.library.service.ContactService;
import com.library.service.LibraryService;
import com.library.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final LibraryService libraryService;
    private final MemberService memberService;
    private final ContactService contactService;

    public AdminController(BookService bookService, LibraryService libraryService,
                            MemberService memberService, ContactService contactService) {
        this.bookService = bookService;
        this.libraryService = libraryService;
        this.memberService = memberService;
        this.contactService = contactService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookService.findAll().size());
        model.addAttribute("totalIssued", libraryService.allIssued().size());
        model.addAttribute("totalMembers", memberService.allUsers().size());
        model.addAttribute("unpaidFines", libraryService.unpaidFines().size());
        model.addAttribute("openMessages", contactService.all().stream().filter(m -> !m.isResolved()).count());
        return "admin/dashboard";
    }

    // ---------------- Books ----------------

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id).orElseThrow());
        return "admin/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        if (book.getId() == null) {
            bookService.addBook(book);
            redirectAttributes.addFlashAttribute("success", "Book added to the catalogue.");
        } else {
            bookService.updateBook(book.getId(), book);
            redirectAttributes.addFlashAttribute("success", "Book updated.");
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("success", "Book removed from the catalogue.");
        return "redirect:/admin/books";
    }

    // ---------------- Issued books ----------------

    @GetMapping("/issued")
    public String issued(Model model) {
        model.addAttribute("records", libraryService.allIssued());
        return "admin/issued";
    }

    // ---------------- Members ----------------

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberService.allUsers());
        return "admin/members";
    }

    @PostMapping("/members/{id}/toggle")
    public String toggleMember(@PathVariable Long id, @RequestParam boolean enabled,
                                RedirectAttributes redirectAttributes) {
        memberService.setEnabled(id, enabled);
        redirectAttributes.addFlashAttribute("success", "Member account updated.");
        return "redirect:/admin/members";
    }

    // ---------------- Fines ----------------

    @GetMapping("/fines")
    public String fines(Model model) {
        model.addAttribute("records", libraryService.unpaidFines());
        return "admin/fines";
    }

    @PostMapping("/fines/{issueId}/paid")
    public String markPaid(@PathVariable Long issueId, RedirectAttributes redirectAttributes) {
        libraryService.markFinePaid(findRecord(issueId));
        redirectAttributes.addFlashAttribute("success", "Fine marked as paid.");
        return "redirect:/admin/fines";
    }

    private IssueRecord findRecord(Long id) {
        return libraryService.unpaidFines().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Record not found"));
    }

    // ---------------- Reservations (read-only overview) ----------------

    @GetMapping("/reservations")
    public String reservations(Model model) {
        model.addAttribute("reservations", libraryService.allReservations());
        return "admin/reservations";
    }

    // ---------------- Contact messages ----------------

    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", contactService.all());
        return "admin/messages";
    }

    @PostMapping("/messages/{id}/resolve")
    public String resolveMessage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactService.markResolved(id);
        redirectAttributes.addFlashAttribute("success", "Message marked as resolved.");
        return "redirect:/admin/messages";
    }
}
