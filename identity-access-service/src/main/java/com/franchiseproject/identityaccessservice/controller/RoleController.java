package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.RoleCreationRequest;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/roles")
//@RequestMapping("/api/v1/roles")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RoleController {
    RoleService roleService;

    // Get all Roles
    @GetMapping
    public ApiResponse<List<Role>> getAllRoles() {
        return ApiResponse.<List<Role>>builder()
                .statusCode(200)
                .message("Gel all role")
                .data(roleService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<Role> getById(@PathVariable UUID id) {
        return ApiResponse.<Role>builder()
                .statusCode(200)
                .message("Get role by id")
                .data(roleService.getById(id))
                .build();
    }

    @GetMapping("/search/{roleName}")
    public ApiResponse<Role> getByName(@PathVariable String roleName) {
        return ApiResponse.<Role>builder()
                .statusCode(200)
                .message("Get role by name")
                .data(roleService.getByName(roleName))
                .build();
    }

    // Create Role
    @PostMapping
    public ApiResponse<Role> createRole(@RequestBody RoleCreationRequest request) {
        return ApiResponse.<Role>builder()
                .statusCode(201)
                .message("Role Created")
                .data(roleService.createRole(request))
                .build();
    }

    // Update Role
    @PutMapping("/{id}")
    public ApiResponse<Role> updateRole(@PathVariable UUID id, @RequestBody RoleCreationRequest role) {
        return ApiResponse.<Role>builder()
                .statusCode(200)
                .message("Role updated")
                .data(roleService.updateRole(id, role))
                .build();
    }

    // Delete Role
    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteRole(@PathVariable UUID id) {
        return ApiResponse.builder()
                .statusCode(200)
                .message("Deleted")
                .data(Boolean.valueOf(roleService.deleteRole(id)))
                .build();
    }

    // Assign Permissions
    @PutMapping("/{id}/permissions")
    public ApiResponse<Role> assignPermissions(
            @PathVariable UUID id,
            @RequestBody @Valid com.franchiseproject.identityaccessservice.dto.request.RolePermissionRequest request) { // <-- Thêm @Valid ở đây

        return ApiResponse.<Role>builder()
                .statusCode(200)
                .message("Assign permissions to Roles successfully.")
                .data(roleService.assignPermissions(id, request))
                .build();
    }
}