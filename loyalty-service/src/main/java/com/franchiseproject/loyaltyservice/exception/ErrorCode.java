package com.franchiseproject.loyaltyservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    INVALID_KEY(1001, "Invalid message key"),
    NOT_FOUND(404, "No Resource Found"),

    // --- CUSTOMER & FRANCHISE ---
    LOYALTY_WALLET_NOT_FOUND(4041, "Loyalty wallet not found for this customer in this franchise"),
    FRANCHISE_ID_REQUIRED(4006, "Franchise ID is required"),
    CUSTOMER_ID_REQUIRED(4007, "Customer ID is required"),

    // --- REDEEM POINTS ---
    INSUFFICIENT_POINTS(4001, "Not enough points to deduct"),
    INVALID_POINTS_AMOUNT(4002, "Points amount must be greater than 0"),

    // --- EARN POINTS ---
    ORDER_AMOUNT_TOO_SMALL(4004, "Order amount is too small to earn points"),
    ORDER_AMOUNT_IS_REQUIRED(4005, "Order amount is required"),

    // --- REFUND POINTS ---
    ORDER_ALREADY_REFUNDED(4014, "Order points have already been refunded"),

    POINTS_IS_REQUIRED(4009, "Points amount is required"),
    POINTS_CANNOT_BE_ZERO(4010, "Adjustment points cannot be zero"),

    // --- SYSTEM CONFIG (TIERS & RULES) ---
    TIER_NAME_REQUIRED(4011, "Tier name is required"),
    REQUIRED_POINTS_REQUIRED(4012, "Required points are required"),
    AMOUNT_PER_POINT_REQUIRED(4013, "Amount per point is required");

    private int code;
    private String message;
}