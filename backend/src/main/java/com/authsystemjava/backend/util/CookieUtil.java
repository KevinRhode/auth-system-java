package com.authsystemjava.backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public void setCookies(HttpServletResponse response,
                           String accessToken,
                           String refreshToken) {
        response.addHeader("Set-Cookie", build("access_token", accessToken, 900));
        response.addHeader("Set-Cookie", build("refresh_token", refreshToken, 604800));
    }

    public void clearCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", build("access_token", "", 0));
        response.addHeader("Set-Cookie", build("refresh_token", "", 0));
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String build(String name, String value, long maxAge) {
        return String.format(
            "%s=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=%d",
            name, value, maxAge
        );
    }
}