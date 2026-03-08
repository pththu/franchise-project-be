package com.franchiseproject.identityaccessservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 6, message = "Code must be 6 digits")
    private String code;
}