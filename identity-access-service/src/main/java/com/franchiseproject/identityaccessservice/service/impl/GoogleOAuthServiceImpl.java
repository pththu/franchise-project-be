package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.client.FranchiseClient;
import com.franchiseproject.identityaccessservice.dto.response.TokenResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import com.franchiseproject.identityaccessservice.service.CognitoService;
import com.franchiseproject.identityaccessservice.service.GoogleOAuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final CognitoService cognitoService;
    final UserMapper userMapper;
    final RestTemplate restTemplate;
    final FranchiseClient franchiseClient;

    @Value("${aws.cognito.domain}")
    private String cognitoDomain;       // https://your-domain.auth.ap-southeast-1.amazoncognito.com

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    @Value("${aws.cognito.googleCallbackUrl}")
    private String callbackUrl;


    @Override
    @Transactional
    public TokenResponse exchangeCodeForTokens(String code) {
        // STEP 1: Exchange code → tokens qua Cognito token endpoint
        Map<String, Object> tokenData = callCognitoTokenEndpoint(code);

        String accessToken  = (String) tokenData.get("access_token");
        String idToken      = (String) tokenData.get("id_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn   = (Integer) tokenData.get("expires_in");
        String tokenType    = (String) tokenData.get("token_type");

        if (accessToken == null) {
            log.error("Cognito token endpoint returned no access_token: {}", tokenData);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // STEP 2: Lấy thông tin user từ Cognito /oauth2/userInfo
        Map<String, String> userInfo = callCognitoUserInfo(accessToken);
        log.info("Google OAuth userInfo: {}", userInfo);

        String cognitoSub = userInfo.get("sub");        // UUID của user trong Cognito
        String email      = userInfo.get("email");
        String fullName   = userInfo.getOrDefault("name", email);
        String username   = userInfo.getOrDefault("cognito:username", cognitoSub);

        if (cognitoSub == null || email == null) {
            log.error("Missing sub or email from Cognito userInfo: {}", userInfo);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // STEP 3: Upsert user vào DB
        User user = upsertUser(cognitoSub, username, email, fullName);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .idToken(idToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn != null ? expiresIn : 3600)
                .tokenType(tokenType != null ? tokenType : "Bearer")
                .user(userMapper.toUserResponse(user, franchiseClient))
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callCognitoTokenEndpoint(String code) {
        String tokenUrl = cognitoDomain + "/oauth2/token";

        // Basic Auth header
        String credentials = clientId + ":" + clientSecret;
        String basicAuth = "Basic " + java.util.Base64.getEncoder()
                .encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("code", code);
        body.add("redirect_uri", callbackUrl);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Cognito token endpoint error: status={}", response.getStatusCode());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            return response.getBody();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to exchange code with Cognito token endpoint: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> callCognitoUserInfo(String accessToken) {
        String userInfoUrl = cognitoDomain + "/oauth2/userInfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            return response.getBody();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get userInfo from Cognito: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private User upsertUser(String cognitoSub, String username, String email, String fullName) {
        UUID userId;
        try {
            userId = UUID.fromString(cognitoSub);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Cognito sub (not UUID): {}", cognitoSub);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        Optional<User> existingById = userRepository.findById(userId);
        if (existingById.isPresent()) {
            User user = existingById.get();
            log.info("Google OAuth: existing user found by sub={}", cognitoSub);
            validateUserStatus(user);
            return user;
        }

        // Lookup theo email (user đã từng đăng ký bằng password rồi login Google)
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            log.info("Google OAuth: existing user found by email={}, linking to Cognito sub={}", email, cognitoSub);
            validateUserStatus(user);
            // Không thay đổi ID (primary key), chỉ đảm bảo verifyEmail = true
            if (!user.isVerifyEmail()) {
                user.setVerifyEmail(true);
                userRepository.save(user);
            }
            return user;
        }

        // Tạo user mới
        log.info("Google OAuth: creating new user — sub={}, email={}", cognitoSub, email);

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        // Username từ Cognito thường có dạng "Google_1234567890" hoặc sub
        // Dùng phần trước @ của email nếu username chứa ký tự không hợp lệ
        String safeUsername = sanitizeUsername(username, email);

        User newUser = User.builder()
                .id(userId)
                .username(safeUsername)
                .email(email)
                .fullName(fullName)
                .phone(null)
                .verifyEmail(true)      // Google đã xác thực email
                .gender(false)          // Mặc định, user có thể cập nhật sau
                .avatarUrl(null)
                .role(customerRole)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(newUser);

        // Đảm bảo có trong Cognito group CUSTOMER
        try {
            cognitoService.addUserToGroup(safeUsername, "CUSTOMER");
        } catch (Exception e) {
            // Log warning nhưng không fail — group có thể sync lại sau
            log.warn("Google OAuth: failed to add user {} to CUSTOMER group: {}", safeUsername, e.getMessage());
        }

        log.info("Google OAuth: new user created — username={}, userId={}", safeUsername, userId);
        return newUser;
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AppException(ErrorCode.USER_lOCKED);
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    private String sanitizeUsername(String username, String email) {
        if (username != null && username.matches("[a-zA-Z0-9._-]+")) {
            return username;
        }
        // Fallback: dùng local part của email, replace ký tự không hợp lệ
        String local = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "_");
        // Tránh trùng username với user đã tồn tại
        if (userRepository.existsByUsername(local)) {
            return local + "_" + System.currentTimeMillis();
        }
        return local;
    }
}
