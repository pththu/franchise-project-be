package com.franchiseproject.loyaltyservice.dto.response;

import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionHistoryResponse {
    UUID id;
    UUID franchiseId;
    UUID promotionId;
    int points;
    int balanceBefore;
    int balanceAfter;
    LoyaltyTransactionType type;
    String description;
    Instant createdAt;
}