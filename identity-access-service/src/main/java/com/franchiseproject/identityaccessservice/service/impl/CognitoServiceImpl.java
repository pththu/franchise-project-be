package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.service.CognitoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoServiceImpl implements CognitoService {

    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;

    /**
     * Đăng ký user mới trên Cognito
     * @return cognitoSub = UUID định danh user trong Cognito
     */
    @Override
    public String registerUser(String username, String password, String email,
                               String fullName, String phone) {
        try {
            Map<String, String> userAttributes = new HashMap<>();
            userAttributes.put("email", email);
            userAttributes.put("name", fullName);
            userAttributes.put("phone_number", normalizePhone(phone));
            // email_verified được set qua flow confirmSignUp

            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .secretHash(computeSecretHash(username))
                    .username(username)
                    .password(password)
                    .userAttributes(
                            userAttributes.entrySet().stream()
                                    .map(e -> AttributeType.builder()
                                            .name(e.getKey())
                                            .value(e.getValue())
                                            .build())
                                    .toList()
                    )
                    .build();

            SignUpResponse response = cognitoClient.signUp(request);
            log.info("Cognito signUp success: username={}, sub={}", username, response.userSub());
            return response.userSub(); // cognitoSub

        } catch (UsernameExistsException e) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        } catch (InvalidPasswordException e) {
            log.info("INVALID_PASSWORD: " + e.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Cognito register error: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VERIFY: Xác thực email bằng OTP code (6 số) gửi qua email
    // ─────────────────────────────────────────────────────────────
    @Override
    public void confirmSignUp(String username, String code) {
        try {
            ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .secretHash(computeSecretHash(username))
                    .username(username)
                    .confirmationCode(code)
                    .build();

            cognitoClient.confirmSignUp(request);
            log.info("Cognito confirmSignUp success: username={}", username);

        } catch (CodeMismatchException e) {
            throw new AppException(ErrorCode.INVALID_CODE);
        } catch (ExpiredCodeException e) {
            throw new AppException(ErrorCode.CODE_EXPRIED);
        } catch (NotAuthorizedException e) {
            throw new RuntimeException("ALREADY_CONFIRMED");
        } catch (Exception e) {
            log.error("Cognito confirmSignUp error: {}", e.getMessage());
            throw new RuntimeException("COGNITO_ERROR: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN: Authenticate và lấy JWT tokens từ Cognito
    // ─────────────────────────────────────────────────────────────
    @Override
    public AuthenticationResultType login(String username, String password) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", username);
            authParams.put("PASSWORD", password);
            authParams.put("SECRET_HASH", computeSecretHash(username));

            InitiateAuthRequest request = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse response = cognitoClient.initiateAuth(request);

            if (response.challengeName() != null) {
                // Handle challenges nếu cần (NEW_PASSWORD_REQUIRED, MFA, v.v.)
                log.warn("Cognito login challenge: {}", response.challengeName());
                throw new RuntimeException("AUTH_CHALLENGE: " + response.challengeName());
            }

            log.info("Cognito login success: username={}", username);
            return response.authenticationResult();

        } catch (NotAuthorizedException e) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        } catch (UserNotConfirmedException e) {
            throw new RuntimeException("USER_NOT_CONFIRMED");
        } catch (UserNotFoundException e) {
            throw new RuntimeException("USER_NOT_FOUND");
        } catch (InvalidLambdaResponseException e) {
            throw new RuntimeException("LAMBDA_ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e; // re-throw known exceptions
        } catch (Exception e) {
            log.error("Cognito login error: {}", e.getMessage());
            throw new RuntimeException("COGNITO_ERROR: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RESEND CODE: Gửi lại code verify
    // ─────────────────────────────────────────────────────────────
    @Override
    public void resendConfirmationCode(String username) {
        try {
            ResendConfirmationCodeRequest request = ResendConfirmationCodeRequest.builder()
                    .clientId(clientId)
                    .secretHash(computeSecretHash(username))
                    .username(username)
                    .build();

            cognitoClient.resendConfirmationCode(request);
            log.info("Resent confirmation code to: {}", username);

        } catch (Exception e) {
            log.error("Resend code error: {}", e.getMessage());
            throw new RuntimeException("RESEND_ERROR: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USER INFO from Cognito by access token
    // ─────────────────────────────────────────────────────────────
    @Override
    public Map<String, String> getUserInfo(String accessToken) {
        GetUserRequest request = GetUserRequest.builder()
                .accessToken(accessToken)
                .build();

        GetUserResponse response = cognitoClient.getUser(request);
        Map<String, String> attrs = new HashMap<>();
        attrs.put("username", response.username());
        response.userAttributes().forEach(a -> attrs.put(a.name(), a.value()));
        return attrs;
    }

    // ─────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────
    @Override
    public AuthenticationResultType refreshToken(String username, String refreshToken) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("REFRESH_TOKEN", refreshToken);
        authParams.put("SECRET_HASH", computeSecretHash(username));

        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        return cognitoClient.initiateAuth(request).authenticationResult();
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN: Set user role via custom attribute or group
    // ─────────────────────────────────────────────────────────────
    @Override
    public void addUserToGroup(String username, String groupName) {
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .groupName(groupName)
                .build();

        cognitoClient.adminAddUserToGroup(request);
        log.info("Added {} to Cognito group: {}", username, groupName);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER: Compute SECRET_HASH (required when App Client has secret)
    // SECRET_HASH = Base64(HMAC-SHA256(username + clientId, clientSecret))
    // ─────────────────────────────────────────────────────────────
    @Override
    public String computeSecretHash(String username) {
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute SECRET_HASH", e);
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        if (phone.startsWith("0")) {
            return "+84" + phone.substring(1);
        }
        return phone.startsWith("+") ? phone : "+" + phone;
    }
}
