package com.franchiseproject.identityaccessservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResponse {
    UUID id;
    String name;
    String api;
    String httpMethod;
    String description;
}