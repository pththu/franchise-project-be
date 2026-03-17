package com.franchiseproject.customerservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    CUSTOMER_NOT_FOUND(404, "Customer not found", HttpStatus.NOT_FOUND),
    INVALID_KEYWORD(400, "Invalid search keyword", HttpStatus.BAD_REQUEST),
    CUSTOMER_ALREADY_EXISTS(1003, "Customer already exists in this franchise", HttpStatus.CONFLICT),
    INVALID_EMAIL_FORMAT(1004, "Invalid email format", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_REQUIRED(1005, "Phone number is required", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatus statusCode;
}