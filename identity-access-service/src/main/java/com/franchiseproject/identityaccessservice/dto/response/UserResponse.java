package com.franchiseproject.identityaccessservice.dto.response;

import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponse {
    UUID id;
    String username;
    String fullName;
    String email;
    boolean isVerifyEmail;
    String phone;
    boolean gender;
    String avatarUrl;
    UUID franchiseId;
    UserStatus status;
    Role role;
    Instant lastLogin;
    Instant createdAt;
}
