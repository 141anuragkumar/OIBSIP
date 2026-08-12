package com.library.service;

import com.library.model.Member;
import com.library.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member register(String name, String email, String password, String phone) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        Member m = new Member();
        m.setName(name);
        m.setEmail(email);
        m.setPassword(passwordEncoder.encode(password));
        m.setPhone(phone);
        m.setRole(Member.Role.USER);
        return memberRepository.save(m);
    }

    public Member getByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    public List<Member> allUsers() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getRole() == Member.Role.USER)
                .toList();
    }

    public void setEnabled(Long id, boolean enabled) {
        Member m = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        m.setEnabled(enabled);
        memberRepository.save(m);
    }
}
