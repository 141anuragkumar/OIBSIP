package com.library.config;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${library.seed.admin-email}")
    private String adminEmail;

    @Value("${library.seed.admin-password}")
    private String adminPassword;

    public DataSeeder(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!memberRepository.existsByEmail(adminEmail)) {
            Member admin = new Member();
            admin.setName("Library Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Member.Role.ADMIN);
            memberRepository.save(admin);
            System.out.println("Seeded default admin account -> " + adminEmail + " / " + adminPassword);
        }
    }
}
