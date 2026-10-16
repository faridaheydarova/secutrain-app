package com.securityawareness.secutrainapp.controller;

import com.securityawareness.secutrainapp.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/mail")
@RequiredArgsConstructor
public class MailTestController {
    private final MailService mailService;

    @PostMapping("/test")
    public ResponseEntity<String> sendTest(@RequestParam String to) {
        mailService.sendSimpleMail(to, "SMTP Test", "This is a test message – the config is working.");
        return ResponseEntity.ok("Sent");
    }
}