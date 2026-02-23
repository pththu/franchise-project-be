package com.franchiseproject.loyaltyservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    INVALID_KEY(1001, "Invalid message key"),
    NOT_FOUND(404, "No Resource Found"),
    CUSTOMER_PROFILE_NOT_FOUND(4041, "Customer loyalty profile not found"),
    INSUFFICIENT_POINTS(4001, "Not enough points to redeem this promotion"),
    PROMOTION_NOT_FOUND(4042, "Promotion not found"),
    PROMOTION_EXPIRED(4002, "Promotion has expired or has not started yet"),
    PROMOTION_OUT_OF_STOCK(4003, "Promotion is out of stock"),
    ORDER_AMOUNT_TOO_SMALL(4004, "Order amount is too small to earn points");
    private int code;
    private String message;
}