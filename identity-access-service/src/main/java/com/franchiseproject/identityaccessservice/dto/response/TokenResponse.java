package com.franchiseproject.identityaccessservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private Integer expiresIn;       // seconds
    private String tokenType;        // "Bearer"
    private UserResponse user;
}

