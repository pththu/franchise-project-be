package com.franchiseproject.identityaccessservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermissionRequest {
    @NotEmpty(message = "Permissions cannot be empty!")
    List<UUID> permissionIds;
}