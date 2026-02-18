package com.franchiseproject.identityaccessservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USERNAME_EXISTED(409, "Username is existed"),
    INVALID_PASSWORD(400, "Password must be at least 8 character"),
    INVALID_KEY(1001, "Invalid message key"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    NOT_FOUND(404, "No Resource Found")
    ;

    private int code;
    private String message;
}
