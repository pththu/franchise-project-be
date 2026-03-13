package com.franchiseproject.deliveryservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    VALIDATION_FAILED(400, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    DELIVERY_NOT_FOUND(404, "Không tìm thấy đơn giao hàng", HttpStatus.NOT_FOUND),
    DELIVERY_ALREADY_FINALIZED(400, "Đơn giao hàng đã được hoàn thành hoặc đánh giá", HttpStatus.BAD_REQUEST),;
    int code;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
