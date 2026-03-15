package com.franchiseproject.paymentservice.enums;

import com.franchiseproject.paymentservice.exception.AppException;
import com.franchiseproject.paymentservice.exception.ErrorCode;

public enum StatusTransaction {
    CREATED,
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    EXPIRED,
    REFUNDED
}
