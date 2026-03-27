package com.franchiseproject.customerservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerTierResponse {
    UUID customerId;
    UUID franchiseId;
    String currentTier;
    Integer currentPoints;
    Integer totalPoints;
}