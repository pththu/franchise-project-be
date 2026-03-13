package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/internal/permissions")
@RequiredArgsConstructor
public class InternalPermissionController {

    private final RoleRepository roleRepository;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @GetMapping("/check")
    public boolean checkPermission(
            @RequestParam String roleName,
            @RequestParam String path,
            @RequestParam String method) {

        // Logic cũ của bạn được dời sang đây
//        return role.getPermissions().stream().anyMatch(permission -> {
//            boolean methodMatch = "ANY".equalsIgnoreCase(permission.getHttpMethod())
//                    || method.equalsIgnoreCase(permission.getHttpMethod());
//            boolean urlMatch = antPathMatcher.match(permission.getApi(), path);
//
//            return methodMatch && urlMatch;
//        });

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
}