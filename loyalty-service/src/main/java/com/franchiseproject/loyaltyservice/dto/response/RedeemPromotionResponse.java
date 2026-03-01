package com.franchiseproject.loyaltyservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedeemPromotionResponse {
    UUID transactionId;
    UUID customerId;
    UUID promotionId;
    Integer pointsDeducted;
    Integer remainingPoints;
    Instant timestamp;
}