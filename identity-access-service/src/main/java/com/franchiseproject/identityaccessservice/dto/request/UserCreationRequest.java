package com.franchiseproject.identityaccessservice.dto.request;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserCreationRequest {
    String username;
    @Size(min = 8, message = "INVALID_PASWORD")
    String password;
    String email;
    String phone;
    boolean gender;
    String avatarUrl;
    UUID franchiseId;
    UUID roleId;
}
