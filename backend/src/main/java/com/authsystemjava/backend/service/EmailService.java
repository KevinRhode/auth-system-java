package com.authsystemjava.backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api-key}")
    private String resendApiKey;

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
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Verify your email — AuthSystem")
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Verification email sent to: {} id: {}", toEmail, response.getId());

        } catch (ResendException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        String link = baseUrl + "/reset-password?token=" + token;

        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
              <h2>Reset your password</h2>
              <p>Hi %s,</p>
              <p>We received a request to reset your password. Click the button below to choose a new one.</p>
              <a href="%s"
                 style="display:inline-block; padding:12px 24px; background:#6366f1;
                        color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                Reset Password
              </a>
              <p>This link expires in 1 hour and can only be used once.</p>
              <p>If you didn't request this, you can safely ignore this email — your password will not change.</p>
              <hr/>
              <p style="color:#94a3b8; font-size:12px;">
                If the button doesn't work, copy this link: %s
              </p>
            </div>
        """.formatted(name, link, link);

        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(toEmail)
                    .subject("Reset your password — AuthSystem")
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Password reset email sent to: {} id: {}", toEmail, response.getId());

        } catch (ResendException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

}