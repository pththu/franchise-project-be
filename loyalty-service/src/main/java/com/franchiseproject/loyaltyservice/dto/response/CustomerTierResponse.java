package com.franchiseproject.loyaltyservice.dto.response;

import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerTierResponse {
    UUID userId;
    CustomerLoyaltyTier loyaltyTier;
    int currentPoint;
    int totalPoint;
}