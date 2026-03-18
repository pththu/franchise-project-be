package com.franchiseproject.loyaltyservice.dto.response;

import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerLoyaltyResponse {
    UUID customerId;
    UUID franchiseId;
    LoyaltyTier loyaltyTier;
    int loyaltyCurrentPoint;
    int loyaltyTotalPoint;
}