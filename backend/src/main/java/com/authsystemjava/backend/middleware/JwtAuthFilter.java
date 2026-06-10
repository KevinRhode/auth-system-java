package com.authsystemjava.backend.middleware;

import com.authsystemjava.backend.service.JwtService;
import com.authsystemjava.backend.util.CookieUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieUtil cookieUtil;
    private final SessionRepository sessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = cookieUtil.getCookieValue(request, "access_token");

        if (token != null) {
            if (jwtService.isTokenExpired(token)) {
                writeUnauthorized(response, "TOKEN_EXPIRED");
                return;
            }

            if (jwtService.isTokenValid(token)) {
                String sessionId = jwtService.extractSessionId(token);

                // session revoked? reject immediately
                if (sessionId == null || !sessionRepository.existsById(sessionId)) {
                    writeUnauthorized(response, "SESSION_REVOKED");
                    return;
                }

                String userId = jwtService.extractUserId(token);
                String role = jwtService.extractRole(token);

                var auth = new UsernamePasswordAuthenticationToken(
                        userId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String error)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + error + "\"}");
    }
}
