package com.franchiseproject.loyaltyservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundPointsRequest {
    @NotNull(message = "CUSTOMER_ID_REQUIRED")
    UUID customerId;

    @NotNull(message = "FRANCHISE_ID_REQUIRED")
    UUID franchiseId;

    @NotNull(message = "ORDER_ID_REQUIRED")
    String orderId;

    @Min(value = 1, message = "POINTS_MUST_BE_GREATER_THAN_ZERO")
    int pointsToRefund;
}