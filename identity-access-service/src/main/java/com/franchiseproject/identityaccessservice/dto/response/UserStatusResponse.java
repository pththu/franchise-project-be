package com.franchiseproject.identityaccessservice.dto.response;

import com.franchiseproject.identityaccessservice.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatusResponse {
    UUID userId;
    UserStatus status;
    boolean isUpdated;
}
