package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    UUID id;
    String fullName;
    String email;
    String phone;
    CustomerStatus status;
    Instant createdAt;
}