package com.authsystemjava.backend.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.email.from}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = baseUrl + "/verify-email?token=" + token;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2>Verify your email</h2>
              <p>Hi %s,</p>
              <p>Thanks for registering. Click the button below to verify your email address.</p>
              <a href="%s"
                 style="display:inline-block; padding:12px 24px; background:#6366f1;
                        color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                Verify Email
              </a>
              <p>This link expires in 24 hours.</p>
              <p>If you didn't create an account, you can ignore this email.</p>
              <hr/>
              <p style="color:#94a3b8; font-size:12px;">
                If the button doesn't work, copy this link: %s
              </p>
            </div>
        """.formatted(name, link, link);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email — AuthSystem");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }
}