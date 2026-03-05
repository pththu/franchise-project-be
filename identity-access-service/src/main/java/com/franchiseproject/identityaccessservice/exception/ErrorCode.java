package com.franchiseproject.identityaccessservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_KEY(400, "Invalid message key"),
    INVALID_PASSWORD(400, "Password must be at least 8 characters"),
    INVALID_VERIFIED_CODE(400, "Invalid verification code"),
    CODE_EXPRIED(400, "Code has expired. Please request a new one"),
    DATA_IS_NULL(400, "Data is null"),
    USERNAME_IS_REQUIRED(400, "Username is required"),
    UNAUTHORIZED(401, "Invalid username or password"),
    FORBIDDEN(403, "Access denied"),
    USER_NOT_CONFIRMED(403, "PENDING_VERIFICATION"),
    NOT_FOUND(404, "No Resource Found"),
    USER_NOT_EXISTED(404, "User is not existed"),
    ROLE_NOT_EXISTED(404, "Role is not existed"),
    ACCOUNT_VERIFIED(409, "Account is already verified"),
    ROLE_EXISTED(409, "Role is existed"),
    USER_EXISTED(409, "User is existed"),
    EMAIL_ALREADY_EXISTS(409, "Email is used"),
    CREATE_USER_FAIL(409, "Email is used"),
    USER_lOCKED(423, "User locked"),
    CREATE_TOKEN_FAIL(500, "Failed to create token"),
    DUPLICATE_KEY(500, "Duplicate key value violates unique constraint"),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized exception"),
    LOGIN_FAILED(400, "Login failed")
    ;

    private int code;
    private String message;
}
