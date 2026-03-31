package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerLoyaltyTier;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerTierResponse {
    UUID userId;
    CustomerLoyaltyTier loyaltyTier;
    int currentPoint;
    int totalPoint;
//    UUID userId;
//    UUID franchiseId;
//    String loyaltyTier;
//    Integer currentPoint;
//    Integer totalPoint;
}