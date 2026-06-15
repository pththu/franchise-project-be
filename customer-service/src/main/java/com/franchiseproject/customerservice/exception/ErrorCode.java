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
    INVALID_KEYWORD(400, "Invalid search keyword", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(400, "Invalid email format", HttpStatus.BAD_REQUEST),
    DATA_NULL(400, "Data is null", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_REQUIRED(400, "Phone number is required", HttpStatus.BAD_REQUEST),
    CUSTOMER_NOT_FOUND(404, "Customer not found", HttpStatus.NOT_FOUND),
    CUSTOMER_ALREADY_EXISTS(409, "Customer already exists in this franchise", HttpStatus.CONFLICT),
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    int code;
    String message;
    HttpStatus statusCode;
}