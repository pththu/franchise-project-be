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

    // ─────────────────────────────────────────────────────────────
    // REGISTER
    // Flow: Validate → Check duplicate → Cognito signUp → Save DB
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public String register(UserRegisterRequest req) {

        // 1. Kiểm tra duplicate trong DB của mình
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findByName(req.getRoleName());

        // 2. Gọi Cognito để tạo user: Cognito sẽ gửi email verification code ngay sau đây
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
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("USERNAME_EXISTS")) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
            log.info("Register failed: ", msg);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // 3. Lưu user vào DB local (status = PENDING_VERIFICATION)
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

        // 4. Trả về username để FE redirect sang màn verify
        return req.getUsername();
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY EMAIL
    // Flow: Cognito confirmSignUp → Update DB status → Done
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void verifyEmail(VerifyRequest req) {

        // 1. Gọi Cognito confirm
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

        // 2. Update status trong DB
        userRepository.findByUsername(req.getUsername()).ifPresent(user -> {
            user.setVerifyEmail(true);
            userRepository.save(user);
            log.info("User verified and activated: {}", req.getUsername());
        });
    }

    // ─────────────────────────────────────────────────────────────
    // RESEND VERIFICATION CODE
    // ─────────────────────────────────────────────────────────────
    public void resendVerificationCode(String username) {
        cognitoService.resendConfirmationCode(username);
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // Flow: Cognito auth → Get user from DB → Build TokenResponse
    // ─────────────────────────────────────────────────────────────
    public TokenResponse login(AuthenticationRequest req) {

        // 1. Kiểm tra user tồn tại trong DB
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // 2. Kiểm tra status
        if (!user.isVerifyEmail()) {
            throw new IllegalStateException("PENDING_VERIFICATION");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new IllegalStateException("Account has been suspended");
        }

        // 3. Authenticate với Cognito
        AuthenticationResultType authResult;
        try {
            authResult = cognitoService.login(req.getUsername(), req.getPassword());
        } catch (RuntimeException e) {
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

    // ─────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────
    @Override
    public TokenResponse refreshToken(String username, String refreshToken) {
        AuthenticationResultType result = cognitoService.refreshToken(username, refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return TokenResponse.builder()
                .accessToken(result.accessToken())
                .idToken(result.idToken())
                .expiresIn(result.expiresIn())
                .tokenType(result.tokenType())
                .user(userMapper.toUserResponse(user))
                .build();
    }

//    @Override
//    public boolean logout() {
//        return true;
//    }
//
//    public AuthenticationResponse login(User user, HttpServletResponse response)
//            throws Exception {
//        var accessToken = generateAccessToken(user.getUsername(), user.getRole().getName());
//        var refeshToken = generateRefreshToken(user.getUsername());
//
//        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refeshToken)
//                .httpOnly(true)
//                .secure(true)
//                .path("/refresh")
//                .maxAge(Duration.ofDays(14))
//                .sameSite("Strict")
//                .build();
//
//        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
//
//        user.setLastLogin(Instant.now());
//        userRepository.save(user);
//
//        return AuthenticationResponse.builder()
//                .authenticated(true)
//                .accessToken(accessToken)
//                .build();
//    }
//
//    public User register(CustomerRegisterRequest request) {
//        if (userRepository.existsByUsername(request.getUsername()))
//            throw new AppException(ErrorCode.USER_EXISTED);
//
//
//        User user = userMapper.toUser(request);
//        user.setStatus(UserStatus.ACTIVE);
//        user.setRole(Role.builder()
//                .id(UUID.fromString("591c1851-fb9b-40f0-86ae-d6b660101d2b"))
//                .build());
//        user.setVerifyEmail(false);
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        return userRepository.save(user);
//    }
//
//    @Override
//    public IntrospectResponse introspect(IntrospectRequest request)
//            throws Exception {
//
//        var token = request.getToken();
//
//        SignedJWT signedJWT = SignedJWT.parse(token);
//
//        // Lấy public key
//        RSAPublicKey publicKey = jwtKeyProperties.getPublicKeyObject();
//
//        JWSVerifier verifier = new RSASSAVerifier(publicKey);
//
//        boolean verified = signedJWT.verify(verifier);
//
//        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//        return IntrospectResponse.builder()
//                .valid(verified && expiryTime.after(new Date()))
//                .build();
//    }
//
//    private String generateAccessToken(String username, String role) throws Exception {
//        Instant now = Instant.now();
//
//        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                .subject(username)
//                .claim("scope", role)
//                .claim("type", "access")
//                .issuer("identity-service")
//                .issueTime(new Date())
//                .expirationTime(new Date(now.plus(10, ChronoUnit.MINUTES).toEpochMilli()))
//                .build();
//
//        return signToken(jwtClaimsSet);
//    }
//
//    private String generateRefreshToken(String username) throws Exception {
//        Instant now = Instant.now();
//        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                .subject(username)
//                .claim("type", "refresh")
//                .issuer("identity-service")
//                .issueTime(new Date())
//                .expirationTime(new Date(now.plus(14, ChronoUnit.DAYS).toEpochMilli()))
//                .build();
//
//        return signToken(jwtClaimsSet);
//    }
//
//    private String signToken(JWTClaimsSet claimsSet) {
//        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
//                .type(JOSEObjectType.JWT)
//                .build();
//        try {
//            SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
//            JWSSigner signer = new RSASSASigner(jwtKeyProperties.getPrivateKeyObject());
//            signedJWT.sign(signer);
//
//            return signedJWT.serialize();
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.CREATE_TOKEN_FAIL);
//        }
//    }
//
//    @Override
//    public UserLockResponse lockUser(UUID userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        user.setStatus(UserStatus.SUSPENDED);
//        userRepository.save(user);
//        return UserLockResponse.builder().isLocked(true).build();
//    }
}
