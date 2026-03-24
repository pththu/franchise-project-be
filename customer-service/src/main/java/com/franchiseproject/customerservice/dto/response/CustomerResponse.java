package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    UUID customerId;
    UserResponse userResponse;
    UUID franchiseId;
    LoyaltyTier loyaltyTier;
    CustomerType type;
    CustomerStatus status;
    Integer loyaltyCurrentPoint;
    Integer loyaltyTotalPoint;
    Instant firstOrderAt;
    Instant lastOrderAt;
    Instant createdAt;
    Instant updatedAt;
}
