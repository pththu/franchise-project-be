package com.franchiseproject.identityaccessservice.dto.request;

import com.franchiseproject.identityaccessservice.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String fullName;
    String roleName;
    UUID franchise;
    UserStatus status;
}
