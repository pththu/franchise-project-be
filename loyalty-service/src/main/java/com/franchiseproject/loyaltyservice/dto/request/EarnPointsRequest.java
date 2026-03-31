package com.franchiseproject.loyaltyservice.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EarnPointsRequest {
    @NotNull(message = "USER_ID_REQUIRED")
    UUID userId;

    @NotNull(message = "ORDER_ID_REQUIRED")
    UUID orderId;

    UUID promotionId;

    @NotNull(message = "FRANCHISE_ID_REQUIRED")
    UUID franchiseId;

    @NotNull(message = "ORDER_AMOUNT_IS_REQUIRED")
    @Min(value = 0, message = "Order amount must be greater than 0")
    Double orderAmount; // Số tiền đơn hàng để tính ra điểm
}