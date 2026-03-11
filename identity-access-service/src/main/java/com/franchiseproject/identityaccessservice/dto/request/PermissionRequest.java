package com.franchiseproject.identityaccessservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
    @NotBlank(message = "API path cannot be empty")
    String api;

    @NotBlank(message = "HTTP Method cannot be empty")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|OPTIONS|ANY)$", message = "HTTP Method invalid (Must be GET, POST, PUT, DELETE, ANY...)")
    String httpMethod;

    String description;
}