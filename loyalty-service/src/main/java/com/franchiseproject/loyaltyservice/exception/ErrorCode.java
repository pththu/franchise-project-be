package com.franchiseproject.loyaltyservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    INVALID_KEY(1001, "Invalid message key"),
    NOT_FOUND(404, "No Resource Found"),
    CUSTOMER_PROFILE_NOT_FOUND(4041, "Customer loyalty profile not found for this franchise"),
    INSUFFICIENT_POINTS(4001, "Not enough points to redeem this promotion"),
    INVALID_POINTS_AMOUNT(4002, "Points amount must be greater than 0"),
    PROMOTION_NOT_FOUND(4042, "Promotion not found"),
    PROMOTION_EXPIRED(4002, "Promotion has expired or has not started yet"),
    PROMOTION_OUT_OF_STOCK(4003, "Promotion is out of stock"),
    ORDER_AMOUNT_TOO_SMALL(4004, "Order amount is too small to earn points"),
    ORDER_AMOUNT_IS_REQUIRED(4005, "Order amount is required"),
    FRANCHISE_ID_REQUIRED(4006, "Franchise ID is required"),
    CUSTOMER_ID_REQUIRED(4007, "Customer ID is required"),
    PROMOTION_ID_REQUIRED(4008, "Promotion ID is required"),
    POINTS_IS_REQUIRED(4009, "Points amount is required"),
    POINTS_CANNOT_BE_ZERO(4010, "Adjustment points cannot be zero"),
    REASON_IS_REQUIRED(4011, "Reason cannot be null");
    private int code;
    private String message;
}