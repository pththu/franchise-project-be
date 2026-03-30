package com.franchiseproject.loyaltyservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyWalletResponse {
    UUID userId;
    UUID franchiseId;
    String currentTier;
    Integer currentPoints;
    Integer totalPoints;
}