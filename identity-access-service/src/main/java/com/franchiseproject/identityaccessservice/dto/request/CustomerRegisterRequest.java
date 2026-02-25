package com.franchiseproject.identityaccessservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerRegisterRequest {
    String username;
    String fullName;
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;
    String email;
    boolean isVerifyEmail;
    String phone;
    boolean gender;
    String avatarUrl;
}
