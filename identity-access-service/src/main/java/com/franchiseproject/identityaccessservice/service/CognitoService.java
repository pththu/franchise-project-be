package com.franchiseproject.identityaccessservice.service;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.Map;

public interface CognitoService {
    String registerUser(String username,
                        String password, String email,
                        String fullName, String phone);

    void confirmSignUp(String username, String code);

    AuthenticationResultType login(String username, String password);

    void resendConfirmationCode(String username);

    Map<String, String> getUserInfo(String accessToken);

    AuthenticationResultType refreshToken(String username, String refreshToken);

    void addUserToGroup(String username, String groupName);

    void removeUserFromGroup(String username, String groupName);

    void disableUser(String username);

    String computeSecretHash(String username);

    void changePassword(String accessToken, String oldPassword, String newPassword);
    void forgotPassword(String username);
    void confirmForgotPassword(String username, String code, String newPassword);

}
