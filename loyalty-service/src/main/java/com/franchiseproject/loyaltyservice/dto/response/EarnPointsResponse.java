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
public class EarnPointsResponse {
    UUID transactionId;
    UUID customerId;
    Integer pointsEarned;
    Integer currentBalance;
    String newTierName;
    Instant timestamp;
}