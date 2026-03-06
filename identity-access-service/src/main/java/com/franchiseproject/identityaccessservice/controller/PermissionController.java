package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.PermissionRequest;
import com.franchiseproject.identityaccessservice.dto.response.PermissionResponse;
import com.franchiseproject.identityaccessservice.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/permissions")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermissionController {

    PermissionService permissionService;

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .statusCode(200)
                .message("Get all permissions success")
                .data(permissionService.getAll())
                .build();
    }

    @PostMapping
    public ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .statusCode(201)
                .message("Permission created")
                .data(permissionService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PermissionResponse> update(@PathVariable UUID id, @RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .statusCode(200)
                .message("Permission updated")
                .data(permissionService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        permissionService.delete(id);
        return ApiResponse.<String>builder()
                .statusCode(200)
                .message("Permission deleted")
                .data("Deleted successfully")
                .build();
    }
}