package com.franchiseproject.orderservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    /// Promotion
    PROMOTION_TIMEOUT(504, "Promotion service timeout", HttpStatus.GATEWAY_TIMEOUT),
    PROMOTION_SERVICE_DOWN(500, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    PROMOTION_NOT_FOUND(404, "Promotion không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_PROMOTION(400, "Mã Promotion sai", HttpStatus.BAD_REQUEST),
    PROMOTION_TRACEBACK_FAILED(400, "Traceback promotion thất bại", HttpStatus.BAD_REQUEST),
    /// Loyalty
    LOYALTY_TIMEOUT(504, "Loyalty service timeout", HttpStatus.GATEWAY_TIMEOUT),
    LOYALTY_SERVICE_DOWN(500, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    LOYALTY_NOT_FOUND(404, "Loyalty không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_LOYALTY(400, "Loyalty không hợp lệ", HttpStatus.BAD_REQUEST),
    LOYALTY_TRACEBACK_FAILED(400, "Traceback loyalty thất bại", HttpStatus.BAD_REQUEST),
    /// Payment
    PAYMENT_INIT_FAILED(400, "Payment init failed", HttpStatus.BAD_REQUEST),
    SYSTEM_ERROR(500, "System error", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_CANCEL(400, "Order này không thể bị hủy", HttpStatus.BAD_REQUEST),
    WRONG_CUSTOMER_ID(404, "Không tìm thấy Id khách hàng", HttpStatus.NOT_FOUND),
    NO_TRANSACTION(404, "Giao dịch thất bại", HttpStatus.NOT_FOUND),
    NO_PRODUCTS(404, "Không có sản phẩm được trả về", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_QUANTITY_PRODUCT(404, "Số lượng sản phẩm không đủ", HttpStatus.NOT_FOUND),
    MISSING_PRODUCTS(404, "Sản phẩm trong đơn hàng không được tìm thấy", HttpStatus.NOT_FOUND),
    ITEM_ORDER_NOT_NULL(404, "Đơn hàng phải có ít nhất một item", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(404, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_FINALIZED(400, "Không thể cập nhật trạng thái của đơn đã kết thúc", HttpStatus.BAD_REQUEST),
    INVALID_SHIPPING_PRICE(400, "Giá ship không hợp lệ", HttpStatus.BAD_REQUEST),
    OUT_OF_STOCK(400, "Sản phẩm đã hết hàng", HttpStatus.BAD_REQUEST),
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
