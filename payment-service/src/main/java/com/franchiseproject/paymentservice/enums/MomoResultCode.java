package com.franchiseproject.paymentservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MomoResultCode {
    SUCCESS(0),
    PENDING_CONFIRM(1000),
    PROCESSING(7000),
    PROVIDER_PROCESSING(7002),
    USER_CANCELLED(1003),
    PARTNER_CANCELLED(1017),
    EXPIRED(1005),
    FAILED_BALANCE(1001),
    FAILED_REJECTED(1002),
    FAILED_LIMIT(1004),
    FAILED_USER_DENY(1006),
    FAILED_ACCOUNT(1007);

    private final int code;

    /// Chuyển các code sang Enums
    public static MomoResultCode fromCode(int code) {
        for (MomoResultCode value : MomoResultCode.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

}
