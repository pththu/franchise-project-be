package com.franchiseproject.customerservice.dto.response;

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
    String fullName;
    String email;
    String phone;
    Instant createdAt;
}