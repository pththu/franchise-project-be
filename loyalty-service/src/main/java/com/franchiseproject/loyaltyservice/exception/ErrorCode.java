package com.franchiseproject.loyaltyservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    INVALID_KEY(1001, "Invalid message key"),
    NOT_FOUND(404, "No Resource Found"),

    // --- Các lỗi của Loyalty & Tier ---
    REWARD_NOT_FOUND(4041, "Reward not found"),
    PROMOTION_NOT_FOUND(4042, "Promotion not found"),
    CUSTOMER_PROFILE_NOT_FOUND(4043, "Customer loyalty profile not found"),
    CUSTOMER_TIER_NOT_FOUND(4044, "Customer does not belong to any tier yet"),
    TIER_NOT_FOUND(4045, "Loyalty Tier not found"),
    TIER_BENEFIT_NOT_FOUND(4046, "Tier Benefit not found"),
    RULE_NOT_FOUND(4047, "Loyalty Rule not found"),
    INSUFFICIENT_POINTS(4001, "Not enough points to redeem this reward"),
    TIER_NAME_EXISTED(4002, "Tier name already exists"),
    INVALID_POINT_RANGE(4003, "Max points must be greater than Min points"),
    INSUFFICIENT_POINTS_BALANCE(4005, "Cannot deduct more points than the current balance");

    private int code;
    private String message;
}