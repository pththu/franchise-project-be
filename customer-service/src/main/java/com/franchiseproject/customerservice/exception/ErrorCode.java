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
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    CUSTOMER_NOT_FOUND(1001, "Khách hàng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_KEY(1002, "Từ khóa tìm kiếm không hợp lệ", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatus statusCode;
}