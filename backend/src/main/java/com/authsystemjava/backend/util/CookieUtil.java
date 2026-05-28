package com.authsystemjava.backend.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.cookie.secure:true}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site:None}")
    private String sameSite;

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
        StringBuilder cookie = new StringBuilder();
        cookie.append(String.format("%s=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=%s",
                name, value, maxAge, sameSite));

        if (secureCookie) {
            cookie.append("; Secure");
        }

        return cookie.toString();
    }
}