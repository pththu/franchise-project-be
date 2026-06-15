package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleOAuthController {
    final GoogleOAuthService googleOAuthService;

    @Value("${aws.cognito.domain}")
    String cognitoDomain;           // e.g. https://your-domain.auth.ap-southeast-1.amazoncognito.com

    @Value("${aws.cognito.clientId}")
    String clientId;

    @Value("${aws.cognito.googleCallbackUrl}")
    String callbackUrl;             // e.g. http://localhost:8080/api/auth/google/callback

    // ─────────────────────────────────────────────────────────────
    // STEP 1 — FE gọi endpoint này để lấy redirect URL
    // ─────────────────────────────────────────────────────────────

    /**
     * Trả về URL để FE redirect người dùng đến trang đăng nhập Google (qua Cognito Hosted UI).
     * FE tự redirect; không dùng HttpServletResponse.sendRedirect() để tránh CORS issue với SPA.
     *
     * Response: { "data": { "redirectUrl": "https://..." } }
     */
    @GetMapping("/login-url")
    public ApiResponse<Map<String, String>> getGoogleLoginUrl() {
        String url = cognitoDomain
                + "/oauth2/authorize"
                + "?identity_provider=Google"
                + "&response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + callbackUrl
                + "&scope=email+openid+profile";

        log.info("Google login URL: {}", url);
        return ApiResponse.<java.util.Map<String, String>>builder()
                .statusCode(200)
                .message("Google login URL generated")
                .data(java.util.Map.of("redirectUrl", url))
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 2 — Cognito callback về đây sau khi Google xác thực xong
    // ─────────────────────────────────────────────────────────────

    /**
     * Cognito redirect về endpoint này với authorization code.
     * BE exchange code → tokens, sync user → set cookie → redirect về FE.
     *
     * Config trong application.yml:
     *   aws.cognito.frontendUrl: http://localhost:3000
     */
    @GetMapping("/callback")
    public void handleGoogleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response,
            @Value("${aws.cognito.frontendUrl}") String frontendUrl
    ) throws IOException {

        if (error != null) {
            log.warn("Google OAuth callback error: {}", error);
            response.sendRedirect(frontendUrl + "/login?error=google_auth_failed");
            return;
        }

        try {
            TokenResponse tokens = googleOAuthService.exchangeCodeForTokens(code);

            // Set access_token cookie
            ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                    .httpOnly(true)
                    .secure(false)          // true khi deploy production
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokens.getExpiresIn()))
                    .sameSite("Lax")        // Lax cho phép cross-site redirect
                    .build();

            // Set refresh_token cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(14))
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // Redirect FE về trang chủ hoặc dashboard
            response.sendRedirect(frontendUrl + "/dashboard");

        } catch (Exception e) {
            log.error("Google OAuth callback failed: {}", e.getMessage(), e);
            response.sendRedirect(frontendUrl + "/login?error=google_auth_failed");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // OPTIONAL — FE SPA: exchange code lấy token qua API (không dùng redirect cookie)
    // ─────────────────────────────────────────────────────────────

    /**
     * Dành cho FE SPA tự handle code trong URL fragment/query param.
     * FE POST code lên đây, nhận về TokenResponse trong body.
     */
    @PostMapping("/exchange")
    public ApiResponse<TokenResponse> exchangeCode(
            @RequestBody java.util.Map<String, String> body,
            HttpServletResponse response
    ) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new com.franchiseproject.identityaccessservice.exception.AppException(
                    com.franchiseproject.identityaccessservice.exception.ErrorCode.IDENTIFIER_IS_REQUIRED);
        }

        TokenResponse tokens = googleOAuthService.exchangeCodeForTokens(code);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", tokens.getAccessToken())
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofSeconds(tokens.getExpiresIn())).sameSite("Strict").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.getRefreshToken())
                .httpOnly(true).secure(false).path("/")
                .maxAge(Duration.ofDays(14)).sameSite("Strict").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.<TokenResponse>builder()
                .statusCode(200)
                .message("Google login successful!")
                .data(tokens)
                .build();
    }
}
