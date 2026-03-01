package com.franchiseproject.orderservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    NO_CANCEL(404, "Order này không thể bị hủy", HttpStatus.BAD_REQUEST),
    WRONG_CUSTOMER_ID(404, "Không tìm thấy Id khách hàng", HttpStatus.NOT_FOUND),
    NO_TRANSACTION(404, "Giao dịch thất bại", HttpStatus.NOT_FOUND),
    NO_PRODUCTS(404,"Không có sản phẩm được trả về", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_QUANTITY_PRODUCT(404, "Số lượng sản phẩm không đủ", HttpStatus.NOT_FOUND),
    MISSING_PRODUCTS(404, "Sản phẩm trong đơn hàng không được tìm thấy", HttpStatus.NOT_FOUND),
    ITEM_ORDER_NOT_NULL(404, "Đơn hàng phải có ít nhất một item", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(404, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_FINALIZED(400, "Không thể cập nhật trạng thái của đơn đã kết thúc", HttpStatus.BAD_REQUEST),
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
