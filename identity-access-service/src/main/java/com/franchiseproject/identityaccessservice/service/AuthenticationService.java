package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.AuthenticationResponse;
import com.franchiseproject.identityaccessservice.dto.response.IntrospectResponse;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserLockResponse;
import com.franchiseproject.identityaccessservice.entity.User;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;
import java.util.UUID;

public interface AuthenticationService {
    boolean logout ();
    TokenResponse refreshToken(UUID userId, String refreshToken);
    TokenResponse login(AuthenticationRequest req);
    String register(UserRegisterRequest req);
    void verifyEmail(VerifyRequest req);
    void resendVerificationCode(String username);
    void changePassword(String accessToken, ChangePasswordRequest request);
    void forgotPassword(String identifier);
    void confirmForgotPassword(ConfirmForgotPasswordRequest request);
}
