package com.resos.shared.security;

import com.resos.shared.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            var claims = jwtTokenProvider.parseClaims(token);
            UUID userId = UUID.fromString(claims.getSubject());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserById(userId);
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BusinessException ex) {
            response.setStatus(mapStatus(ex.getCode()));
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error":{"code":"%s","message":"%s"}}\
                    """.formatted(ex.getCode(), ex.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int mapStatus(String code) {
        return switch (code) {
            case "TOKEN_EXPIRED", "UNAUTHENTICATED" -> HttpServletResponse.SC_UNAUTHORIZED;
            default -> HttpServletResponse.SC_BAD_REQUEST;
        };
    }
}
