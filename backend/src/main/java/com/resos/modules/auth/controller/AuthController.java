package com.resos.modules.auth.controller;

import com.resos.config.JwtProperties;
import com.resos.modules.auth.dto.*;
import com.resos.modules.auth.service.AuthService;
import com.resos.modules.user.dto.UserResponse;
import com.resos.modules.user.service.UserService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthService.AuthResult result = authService.register(
                request, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        setRefreshCookie(httpResponse, result.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result.response()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthService.AuthResult result = authService.login(
                request, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        setRefreshCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.of(result.response()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String rawToken = resolveRefreshToken(body, httpRequest);
        AuthService.AuthResult result = authService.refresh(
                rawToken, httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        setRefreshCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(ApiResponse.of(result.response()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest body,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String rawToken = resolveRefreshToken(body, httpRequest);
        AuthService.UserPrincipalHolder holder = principal != null
                ? new AuthService.UserPrincipalHolder(principal.getId()) : null;
        authService.logout(rawToken, holder);
        clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(ApiResponse.of(userService.getCurrentUser(principal)));
    }

    private String resolveRefreshToken(RefreshTokenRequest body, HttpServletRequest request) {
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return body.refreshToken();
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtProperties.getRefreshCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new com.resos.shared.exception.BusinessException("UNAUTHENTICATED", "Refresh token is required");
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(jwtProperties.getRefreshCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production behind HTTPS
        cookie.setPath(jwtProperties.getRefreshCookiePath());
        cookie.setMaxAge((int) jwtProperties.getRefreshTokenExpiration().getSeconds());
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtProperties.getRefreshCookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setPath(jwtProperties.getRefreshCookiePath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
