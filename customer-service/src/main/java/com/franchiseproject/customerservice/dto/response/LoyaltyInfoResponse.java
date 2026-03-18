package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.LoyaltyTier;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyInfoResponse {
    UUID id;
    UUID franchiseId;
    LoyaltyTier loyaltyTier;
    int loyaltyCurrentPoint;
    int loyaltyTotalPoint;
}