package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalPermissionController {

    UserMapper userMapper;
    UserService userService;
    RoleRepository roleRepository;
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @GetMapping("/permissions/check")
    public boolean checkPermission(
            @RequestParam String roleName,
            @RequestParam String path,
            @RequestParam String method) {

        log.info("roleName {}", roleName);
        if (roleName.isEmpty()) {
            return false;
        }

        Role role = roleRepository.findByName(roleName).orElse(null);
        if (role == null || role.getPermissions() == null || role.getPermissions().isEmpty()) {
            return false;
        }

        log.info("role: {}", role);

        boolean isAllowed = role.getPermissions().stream().anyMatch(permission -> {
            boolean methodMatch = "ANY".equalsIgnoreCase(permission.getHttpMethod())
                    || method.equalsIgnoreCase(permission.getHttpMethod());
            boolean urlMatch = antPathMatcher.match(permission.getApi(), path);

            return methodMatch && urlMatch;
        });

        if (isAllowed) {
            log.info("Access GRANTED: Role [{}] truy cập [{}] {}", roleName, method, path);
            return true;
        } else {
            log.warn("Access DENIED: Role [{}] bị chặn khi cố truy cập [{}] {}", roleName, method, path);
        }

        return false;
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID userId) {
        User user = userService.getById(userId);
        log.info("getUserById: userId={}, verifyEmail={}", userId, user.isVerifyEmail());
        return ApiResponse.<UserResponse>builder()
                .statusCode(200)
                .message("Get One")
                .data(userMapper.toUserResponse(user))
                .build();
    }

    @PostMapping("/users/search-by-ids")
    public ApiResponse<List<UserResponse>> getUsersByIdsInternal(@RequestBody List<UUID> userIds) {
        var users = userService.getUsersByIds(userIds);

        List<UserResponse> userResponses = users.stream()
                .map(userMapper::toUserResponse)
                .toList();

        return ApiResponse.<List<UserResponse>>builder()
                .statusCode(200)
                .message("Get users successfully")
                .data(userResponses)
                .build();
    }
}