package com.franchiseproject.identityaccessservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ROLE_EXISTED(409, "Role is existed"),
    USER_EXISTED(409, "User is existed"),
    INVALID_PASSWORD(400, "Password must be at least 8 character"),
    INVALID_KEY(1001, "Invalid message key"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    NOT_FOUND(404, "No Resource Found"),
    USER_NOT_EXISTED(404, "User is not existed"),
    UNAUTHORIZED(401, "Invalid username or password"),
    CREATE_TOKEN_FAIL(500, "Failed to create token");

    private int code;
    private String message;
}
