package com.franchiseproject.customerservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncCustomerRequest {
    UUID id;
    String fullName;
    String email;
    String phone;
}