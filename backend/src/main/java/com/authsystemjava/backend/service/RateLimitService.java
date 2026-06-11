package com.authsystemjava.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(2))
            .maximumSize(100_000)
            .build();

    /** 5 attempts per minute per IP — login */
    public boolean tryConsumeLogin(String ip) {
        return resolve("login:" + ip,
                Bandwidth.builder().capacity(5)
                        .refillGreedy(5, Duration.ofMinutes(1)).build())
                .tryConsume(1);
    }

    /** 3 registrations per hour per IP */
    public boolean tryConsumeRegister(String ip) {
        return resolve("register:" + ip,
                Bandwidth.builder().capacity(3)
                        .refillGreedy(3, Duration.ofHours(1)).build())
                .tryConsume(1);
    }

    /** 2 resends per hour per email */
    public boolean tryConsumeResend(String email) {
        return resolve("resend:" + email.toLowerCase(),
                Bandwidth.builder().capacity(2)
                        .refillGreedy(2, Duration.ofHours(1)).build())
                .tryConsume(1);
    }

    /** 2 password-reset emails per hour per email */
    public boolean tryConsumeForgotPassword(String email) {
        return resolve("forgot:" + email.toLowerCase(),
                Bandwidth.builder().capacity(2)
                        .refillGreedy(2, Duration.ofHours(1)).build())
                .tryConsume(1);
    }

    /** 5 forgot-password requests per hour per IP */
    public boolean tryConsumeForgotPasswordIp(String ip) {
        return resolve("forgot-ip:" + ip,
                Bandwidth.builder().capacity(5)
                        .refillGreedy(5, Duration.ofHours(1)).build())
                .tryConsume(1);
    }

    /** 5 reset attempts per hour per IP — slows token guessing */
    public boolean tryConsumeResetPassword(String ip) {
        return resolve("reset:" + ip,
                Bandwidth.builder().capacity(5)
                        .refillGreedy(5, Duration.ofHours(1)).build())
                .tryConsume(1);
    }

    private Bucket resolve(String key, Bandwidth limit) {
        return buckets.get(key, k -> Bucket.builder().addLimit(limit).build());
    }
}