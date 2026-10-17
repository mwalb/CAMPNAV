package com.campnav.backend.controller;

import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:charlesnestory793@gmail.com}")
    private String contactRecipientEmail;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContactInquiry(@RequestBody ContactRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Name is required."));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() || !request.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "A valid email address is required."));
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Message is required."));
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contact inquiry received (email delivery disabled)."
            ));
        }

        String subj = request.getSubject() != null && !request.getSubject().trim().isEmpty()
                ? "[CAMPNAV Contact] " + request.getSubject().trim()
                : "[CAMPNAV Contact] Inquiry from " + request.getName().trim();

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f8fafc;'>" +
                "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; padding: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h2 style='color: #1E3A8A; margin-top: 0;'>New Contact Inquiry</h2>" +
                "<p><strong>Name:</strong> " + escapeHtml(request.getName()) + "</p>" +
                "<p><strong>Email:</strong> " + escapeHtml(request.getEmail()) + "</p>" +
                "<p><strong>Subject:</strong> " + escapeHtml(request.getSubject() != null ? request.getSubject() : "N/A") + "</p>" +
                "<hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 16px 0;'/>" +
                "<p><strong>Message:</strong></p>" +
                "<p style='background: #f1f5f9; padding: 12px; border-radius: 6px; white-space: pre-wrap;'>" + escapeHtml(request.getMessage()) + "</p>" +
                "</div></div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String recipient = (contactRecipientEmail != null && !contactRecipientEmail.isBlank())
                    ? contactRecipientEmail : "charlesnestory793@gmail.com";
            helper.setTo(recipient);
            helper.setReplyTo(request.getEmail().trim());
            helper.setSubject(subj);
            helper.setText(request.getMessage(), htmlBody);
            mailSender.send(message);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contact inquiry successfully sent."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to deliver email: " + e.getMessage()
            ));
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Data
    public static class ContactRequest {
        private String name;
        private String email;
        private String subject;
        private String message;
    }
}
