package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.client.FranchiseClient;
import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.FranchiseResponse;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
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

import java.time.Instant;
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
    FranchiseClient franchiseClient;

    /**
     * Register luôn gắn role = CUSTOMER.
     * User chưa được add vào Cognito group ở bước này — phải verify email trước.
     */
    @Transactional
    public String register(UserRegisterRequest req) {

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_IS_EXISTS);
        }

        // Register luôn là CUSTOMER — bỏ qua roleName từ request nếu có
        Role role = roleRepository.findByName("CUSTOMER")
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
                .verifyEmail(false)
                .gender(req.isGender())
                .avatarUrl(req.getAvatarUrl())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("User registered: username={}, cognitoSub={}", user.getUsername(), cognitoSub);
        return req.getUsername();
    }

    /**
     * Verify email:
     * 1. Confirm với Cognito
     * 2. Add user vào Cognito group theo role trong DB (luôn là CUSTOMER với flow register thông thường)
     * 3. Cập nhật verifyEmail = true trong DB
     */
    @Transactional
    public void verifyEmail(VerifyRequest req) {

        // 1. Confirm email với Cognito
        try {
            cognitoService.confirmSignUp(req.getUsername(), req.getCode());
        } catch (AppException e) {
            throw e;
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

        // 3. Add user vào Cognito group theo role trong DB: Với flow register thông thường, role luôn là CUSTOMER
        cognitoService.addUserToGroup(user.getUsername(), user.getRole().getName());

        // 4. Cập nhật trạng thái verify
        user.setVerifyEmail(true);
        userRepository.save(user);

        log.info("User verified and added to Cognito group '{}': username={}",
                user.getRole().getName(), user.getUsername());
    }

    public void resendVerificationCode(String username) {
        cognitoService.resendConfirmationCode(username);
    }

    @Override
    public void changePassword(String accessToken, ChangePasswordRequest request) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        log.info("changePassword: forwarding to Cognito ChangePassword API");
        // Delegate hoàn toàn sang Cognito — Cognito validate oldPassword và enforce policy
        cognitoService.changePassword(accessToken, request.getOldPassword(), request.getNewPassword());
        log.info("changePassword: Cognito ChangePassword success");
    }

    @Override
    public void forgotPassword(String identifier) {
        log.info("forgotPassword: identifier={}", identifier);

        String username = resolveUsername(identifier);

        // Nếu không tìm thấy user → return silently (chống user enumeration)
        if (username == null) {
            log.warn("forgotPassword: user not found for identifier={}, returning silently", identifier);
            return;
        }

        // Kiểm tra user có thể reset password không
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getStatus() == UserStatus.SUSPENDED) {
                throw new AppException(ErrorCode.USER_lOCKED);
            }
            if (user.getStatus() == UserStatus.INACTIVE) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }
        });

        cognitoService.forgotPassword(username);
        log.info("forgotPassword: OTP sent for username={}", username);
    }

    @Override
    public void confirmForgotPassword(ConfirmForgotPasswordRequest request) {
        log.info("confirmForgotPassword: username={}", request.getIdentifier());

        User user = userRepository.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        cognitoService.confirmForgotPassword(
                user.getUsername(),
                request.getCode(),
                request.getNewPassword()
        );

        log.info("confirmForgotPassword: success for username={}", user.getUsername());
    }

    public TokenResponse login(AuthenticationRequest req) {

        String identifier = req.getIdentifier();
        String password = req.getPassword();

        log.info("identifier: {}", identifier);
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        log.info("user email: {}, username: {}", user.getEmail(), user.getUsername());

        // Kiểm tra status
        if (!user.isVerifyEmail()) {
            throw new AppException(ErrorCode.USER_NOT_CONFIRMED);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AppException(ErrorCode.USER_lOCKED);
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Authenticate với Cognito
        AuthenticationResultType authResult;
        try {
            authResult = cognitoService.login(user.getUsername(), password);
        } catch (RuntimeException e) {
            log.info("Login exception: ", e);
            String msg = e.getMessage();
            if (msg.contains("INVALID_CREDENTIALS")) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            if (msg.contains("USER_NOT_CONFIRMED")) {
                throw new AppException(ErrorCode.USER_NOT_CONFIRMED);
            }
            log.error("Login failed: {}", msg);
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(authResult.accessToken())
                .idToken(authResult.idToken())
                .refreshToken(authResult.refreshToken())
                .expiresIn(authResult.expiresIn())
                .tokenType(authResult.tokenType())
                .user(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    @Override
    public TokenResponse refreshToken(UUID userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        AuthenticationResultType result = cognitoService.refreshToken(user.getUsername(), refreshToken);

//        UUID franchiseId = user.getFranchiseId();
//        UserResponse userResponse = userMapper.toUserResponse(user);
//        if (franchiseId != null) {
//            FranchiseResponse franchiseResponse = franchiseClient.getFranchiseById(franchiseId);
//            if (franchiseResponse != null) {
//                userResponse.setFranchise(franchiseResponse);
//            }
//        }

        return TokenResponse.builder()
                .accessToken(result.accessToken())
                .idToken(result.idToken())
                .expiresIn(result.expiresIn())
                .tokenType(result.tokenType())
                .user(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    @Override
    public boolean logout() {
        return true;
    }

    private String resolveUsername(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;

        // Nếu chứa @ → coi là email
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .map(User::getUsername)
                    .orElse(null);
        }

        // Kiểm tra username có tồn tại không
        return userRepository.findByUsername(identifier)
                .map(User::getUsername)
                .orElse(null);
    }
}
