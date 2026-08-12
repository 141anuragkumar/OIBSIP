package com.library.controller;

import com.library.model.Member;
import com.library.service.ContactService;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    private final MemberService memberService;
    private final ContactService contactService;

    public PublicController(MemberService memberService, ContactService contactService) {
        this.memberService = memberService;
        this.contactService = contactService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Sends admins to the admin dashboard and members to the catalogue after login. */
    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        return "redirect:" + (isAdmin ? "/admin/dashboard" : "/catalogue");
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("member", new Member());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam(required = false) String phone,
                            Model model) {
        try {
            memberService.register(name, email, password, phone);
            model.addAttribute("success", "Account created — you can now log in.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/contact")
    public String contactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String message,
                                 Model model) {
        contactService.submit(name, email, message);
        model.addAttribute("success", "Thanks — your message has been sent to the library team.");
        return "contact";
    }
}
