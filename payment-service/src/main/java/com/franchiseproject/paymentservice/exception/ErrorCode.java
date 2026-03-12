package com.franchiseproject.paymentservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    NOT_FOUND_TRANSACTION(404, "Transaction not found", HttpStatus.NOT_FOUND),
    SIGNATURE_FAILED(400, "tạo signature thất bại", HttpStatus.BAD_REQUEST),
    INVALID_SIGNATURE(400, "Invalid signature", HttpStatus.BAD_REQUEST),
    PAYMENT_METHOD_NOT_SUPPORTED(400, "Phương thức thanh toán không được hỗ trợ", HttpStatus.BAD_REQUEST),
    ORDER_NOT_PAYABLE(409, "Order không yêu cầu thanh toán", HttpStatus.CONFLICT),
    NOT_FOUND_ORDER(404, "Không Tìm Thấy Đơn Hàng", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_AVAILABLE(409, "Method đang bị khóa", HttpStatus.CONFLICT),
    VALIDATION_FAILED(400, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST);
    int code;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
