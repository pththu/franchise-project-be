package com.franchiseproject.loyaltyservice.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemPromotionRequest {
    @NotNull(message = "CUSTOMER_ID_REQUIRED")
    UUID customerId;

    @NotNull(message = "FRANCHISE_ID_REQUIRED")
    UUID franchiseId;

    @NotNull(message = "PROMOTION_ID_REQUIRED")
    UUID promotionId;

    @NotNull(message = "POINTS_IS_REQUIRED")
    Integer pointsToRedeem;
}