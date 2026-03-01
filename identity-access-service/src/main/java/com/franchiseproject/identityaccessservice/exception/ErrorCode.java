package com.franchiseproject.identityaccessservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ROLE_EXISTED(409, "Role is existed"),
    USER_EXISTED(409, "User is existed"),
    EMAIL_ALREADY_EXISTS(409, "Email is used"),
    CREATE_USER_FAIL(409, "Email is used"),
    INVALID_PASSWORD(400, "Password must be at least 8 characters"),
    INVALID_KEY(1001, "Invalid message key"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    DUPLICATE_KEY(500, "Duplicate key value violates unique constraint"),
    FORBIDDEN(403, "Access denied"),
    NOT_FOUND(404, "No Resource Found"),
    USER_NOT_EXISTED(404, "User is not existed"),
    ROLE_NOT_EXISTED(404, "Role is not existed"),
    UNAUTHORIZED(401, "Invalid username or password"),
    CREATE_TOKEN_FAIL(500, "Failed to create token"),
    DATA_IS_NULL(400, "Data is null")
    ;

    private int code;
    private String message;
}
