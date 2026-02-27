package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.AuthenticationRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.IntrospectRequest;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;
import com.franchiseproject.identityaccessservice.dto.response.IntrospectResponse;
import com.franchiseproject.identityaccessservice.entity.User;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody @Valid CustomerRegisterRequest request) {
        return ApiResponse.<User>builder()
                .statusCode(201)
                .message("Register success")
                .data(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request, HttpServletResponse response)
            throws Exception {

        AuthenticationResponse authResponse = authenticationService.login(request, response);
        ResponseCookie cookie = ResponseCookie.from("access_token", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(200)
                .message("Login success")
                .data(authResponse)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws Exception {
        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(200)
                .message("Login success")
                .data(authenticationService.introspect(request))
                .build();
    }
}
