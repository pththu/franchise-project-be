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
public class AdjustPointsResponse {
    UUID transactionId;
    UUID customerId;
    Integer pointsAdjusted;
    Integer balanceBefore;
    Integer balanceAfter;
    Instant timestamp;
}