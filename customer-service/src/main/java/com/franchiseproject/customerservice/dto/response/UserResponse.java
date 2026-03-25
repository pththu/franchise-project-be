package com.franchiseproject.customerservice.dto.response;

import com.franchiseproject.customerservice.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String fullName;
    String email;
    boolean verifyEmail;
    String phone;
    boolean gender;
    String avatarUrl;
    UUID franchiseId;
    UserStatus status;
    RoleResponse role;
    Instant lastLogin;
    Instant createdAt;
}
