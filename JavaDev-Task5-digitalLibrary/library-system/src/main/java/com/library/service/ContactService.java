package com.library.service;

import com.library.model.ContactMessage;
import com.library.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactMessageRepository repository;

    public ContactService(ContactMessageRepository repository) {
        this.repository = repository;
    }

    public ContactMessage submit(String name, String email, String message) {
        ContactMessage cm = new ContactMessage();
        cm.setName(name);
        cm.setEmail(email);
        cm.setMessage(message);
        return repository.save(cm);
    }

    public List<ContactMessage> all() {
        return repository.findAllByOrderBySubmittedAtDesc();
    }

    public void markResolved(Long id) {
        ContactMessage cm = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        cm.setResolved(true);
        repository.save(cm);
    }
}
