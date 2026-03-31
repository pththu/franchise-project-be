package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerFranchiseResponse {
    UUID id;
//    UUID franchiseId;
    UserResponse user;
//    UUID userId;
//    UUID franchiseId;
//    UserResponse userResponse;
    CustomerType type;
    CustomerStatus status;
    Instant firstOrderAt;
    Instant lastOrderAt;
    FranchiseResponse franchise;
    Instant createdAt;
    Instant updatedAt;
//    CustomerTierResponse loyaltyInfo;
}