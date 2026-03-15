package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.AuthenticationService;
import com.franchiseproject.identityaccessservice.service.CognitoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.UUID;

@Slf4j
@CommonsLog
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    CognitoService cognitoService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Transactional
    public String register(UserRegisterRequest req) {

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_IS_EXISTS);
        }

        Role role = roleRepository.findByName(req.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        String cognitoSub;
        try {
            cognitoSub = cognitoService.registerUser(
                    req.getUsername(),
                    req.getPassword(),
                    req.getEmail(),
                    req.getFullName(),
                    req.getPhone()
            );

            log.info("cognitoSub: {}", cognitoSub);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Register failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        User user = User.builder()
                .id(UUID.fromString(cognitoSub))
                .username(req.getUsername())
                .email(req.getEmail())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .isVerifyEmail(false)
                .gender(req.isGender())
                .avatarUrl(req.getAvatarUrl())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("User registered: username={}, cognitoSub={}", user.getUsername(), cognitoSub);
        return req.getUsername();
    }

    @Transactional
    public void verifyEmail(VerifyRequest req) {

        // 1. Confirm email với Cognito
        try {
            cognitoService.confirmSignUp(req.getUsername(), req.getCode());
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            switch (msg) {
                case "INVALID_CODE" -> throw new AppException(ErrorCode.INVALID_VERIFIED_CODE);
                case "EXPIRED_CODE" -> throw new AppException(ErrorCode.CODE_EXPRIED);
                case "ALREADY_CONFIRMED" -> throw new AppException(ErrorCode.ACCOUNT_VERIFIED);
                default -> throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        // 2. Lấy user từ DB
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 3. Add user vào Cognito group theo role
        cognitoService.addUserToGroup(user.getUsername(), user.getRole().getName());

        // 4. Update trạng thái verify
        user.setVerifyEmail(true);
        userRepository.save(user);

        log.info("User verified and added to group: {}", user.getUsername());
    }

    public void resendVerificationCode(String username) {
        cognitoService.resendConfirmationCode(username);
    }

    public TokenResponse login(AuthenticationRequest req) {

        String identifier = req.getIdentifier();
        String password = req.getPassword();
        // 1. Kiểm tra user tồn tại trong DB
        log.info("identitfier: " + identifier);
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        log.info("identitfier 02" + identifier);
        log.info("user 02" + user.getEmail());
        log.info("user getUsername: " + user.getUsername());

        // 2. Kiểm tra status
        if (!user.isVerifyEmail()) {
            throw new AppException(ErrorCode.USER_NOT_CONFIRMED);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AppException(ErrorCode.USER_lOCKED);
        }

        // 3. Authenticate với Cognito
        AuthenticationResultType authResult;
        try {
            authResult = cognitoService.login(user.getUsername(), password);
        } catch (RuntimeException e) {
            log.info("e: ", e);
            String msg = e.getMessage();
            if (msg.contains("INVALID_CREDENTIALS")) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            if (msg.contains("USER_NOT_CONFIRMED")) {
                throw new AppException(ErrorCode.USER_NOT_CONFIRMED);
            }
            log.info("Login failed: ", msg);
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        // 4. Build response với tokens từ Cognito + user info từ DB
        return TokenResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .expiresIn(authResult.expiresIn())
                .tokenType(authResult.tokenType())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public TokenResponse refreshToken(UUID userId, String refreshToken) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AuthenticationResultType result = cognitoService.refreshToken(user.getUsername(), refreshToken);
        return TokenResponse.builder()
                .accessToken(result.accessToken())
                .idToken(result.idToken())
                .expiresIn(result.expiresIn())
                .tokenType(result.tokenType())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public boolean logout() {
        return true;
    }

}
