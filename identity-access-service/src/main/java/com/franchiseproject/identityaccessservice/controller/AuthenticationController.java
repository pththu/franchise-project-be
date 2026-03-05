package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;
import com.franchiseproject.identityaccessservice.dto.response.IntrospectResponse;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserLockResponse;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.service.AuthenticationService;
import com.franchiseproject.identityaccessservice.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
//@RequestMapping("/api/v1/auth")
@RequestMapping("/api/auth")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;
    PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/register
     * Đăng ký user mới → Cognito → DB
     * Response: username để FE redirect sang /verify
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@Valid @RequestBody UserRegisterRequest req) {
        String username = authenticationService.register(req);
        return ApiResponse.<Map<String, String>>builder()
                .statusCode(201)
                .message("Registration successful! Please check your email for verification code.")
                .data(Map.of("username", username))
                .build();
    }

    /**
     * POST /api/auth/verify
     * Verify email với OTP code từ Cognito
     */
    @PostMapping("/verify")
    public ApiResponse verify(
            @Valid @RequestBody VerifyRequest request) {

        authenticationService.verifyEmail(request);
        return ApiResponse.builder()
                .statusCode(200)
                .message("Email verified successfully! You can now sign in.")
                .build();
    }

    /**
     * POST /api/auth/resend-code
     * Gửi lại verification code
     */
    @PostMapping("/resend-code")
    public ApiResponse resendCode(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        if (username == null || username.isBlank()) {
            throw new AppException(ErrorCode.USERNAME_IS_REQUIRED);
        }

        authenticationService.resendVerificationCode(username);
        return ApiResponse.builder()
                .statusCode(200)
                .message("Verification code sent to your email.")
                .build();
    }

    /**
     * POST /api/auth/login
     * Đăng nhập → trả về access_token, id_token, refresh_token
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody AuthenticationRequest request) {

        TokenResponse tokens = authenticationService.login(request);
        return ApiResponse.<TokenResponse>builder()
                .statusCode(200)
                .message("Login successful!")
                .data(tokens)
                .build();
    }

    /**
     * POST /api/auth/refresh
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String refreshToken = body.get("refreshToken");

        TokenResponse tokens = authenticationService.refreshToken(username, refreshToken);
        return ApiResponse.<TokenResponse>builder()
                .statusCode(200)
                .message("Token refreshed.")
                .data(tokens)
                .build();
    }

//    @PostMapping("/register")
//    public ApiResponse<User> register(@RequestBody @Valid CustomerRegisterRequest request) {
//        return ApiResponse.<User>builder()
//                .statusCode(201)
//                .message("Register success")
//                .data(authenticationService.register(request))
//                .build();
//    }
//
//    @PostMapping("/login")
//    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request, HttpServletResponse response)
//            throws Exception {
//
//        User user = userService.getByUsername(request.getUsername());
//
//        if (user == null) {
//            throw new AppException(ErrorCode.USER_NOT_EXISTED);
//        }
//
//        boolean result = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
//        if (!result) throw new AppException(ErrorCode.UNAUTHORIZED);
//
//        if (user.getStatus().equals(UserStatus.DELETED)) {
//            throw new AppException(ErrorCode.USER_NOT_EXISTED);
//        }
//
//        if (user.getStatus().equals(UserStatus.SUSPENDED)) {
//            throw new AppException(ErrorCode.USER_lOCKED);
//        }
//
//        AuthenticationResponse authResponse = authenticationService.login(user, response);
//        ResponseCookie cookie = ResponseCookie.from("access_token", authResponse.getAccessToken())
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .maxAge(Duration.ofMinutes(10))
//                .sameSite("Strict")
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//        return ApiResponse.<AuthenticationResponse>builder()
//                .statusCode(200)
//                .message("Login success")
//                .data(authResponse)
//                .build();
//    }
//
//    @PostMapping("/introspect")
//    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
//            throws Exception {
//        return ApiResponse.<IntrospectResponse>builder()
//                .statusCode(200)
//                .message("Login success")
//                .data(authenticationService.introspect(request))
//                .build();
//    }
//
//    @GetMapping("/logout")
//    public ApiResponse<String> logout(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) {
//
//        String username = jwt.getSubject();
//        User user = userService.getByUsername(username);
//        if (user == null) {
//            throw new AppException(ErrorCode.USER_NOT_EXISTED);
//        }
//
//        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
//                .httpOnly(true)
//                .secure(true)
//                .path("/")
//                .maxAge(0)
//                .sameSite("Strict")
//                .build();
//
//        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
//                .httpOnly(true)
//                .secure(true)
//                .path("/refresh")
//                .maxAge(0)
//                .sameSite("Strict")
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
//        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
//
//        return ApiResponse.<String>builder()
//                .statusCode(200)
//                .message("Logout")
//                .data("Logout: " + authenticationService.logout())
//                .build();
//    }
//
//    @PutMapping("/{userId}/lock")
//    public ApiResponse<UserLockResponse> lockUser(@PathVariable UUID userId) {
//        return ApiResponse.<UserLockResponse>builder()
//                .statusCode(200)
//                .message("User locked")
//                .data(authenticationService.lockUser(userId))
//                .build();
//    }
}
