package com.franchiseproject.identityaccessservice.dto.request;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UserCreationRequest {
    String username;
    String fullName;
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;
    String email;
    boolean isVerifyEmail;
    String phone;
    boolean gender;
    String avatarUrl;
    UUID franchiseId;
    UUID roleId;
}
