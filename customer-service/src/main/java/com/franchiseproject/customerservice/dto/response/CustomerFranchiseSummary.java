package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerFranchiseSummary {
    FranchiseResponse franchise; // tên, địa chỉ, phone… từ Franchise Service
    CustomerStatus status;       // ACTIVE / INACTIVE / SUSPENDED tại franchise này
    Instant firstOrderAt;
    Instant lastOrderAt;
    Instant createdAt;
}
