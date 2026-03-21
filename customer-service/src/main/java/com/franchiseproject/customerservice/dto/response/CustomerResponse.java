package com.franchiseproject.customerservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {
    UUID id;
    String username;
    String fullName;
    String email;
    boolean verifyEmail;
    String phone;
    boolean gender;
    String avatarUrl;
    UUID franchiseId;
    String status;
    Instant lastLogin;
    Instant createdAt;
}
