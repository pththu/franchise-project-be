package com.franchiseproject.loyaltyservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyReportResponse {
    Long totalPointsEarned;
    Long totalPointsRedeemed;
    Long totalEarnTransactions;
    Long totalRedeemTransactions;
    Map<String, Long> customerByTier;
}