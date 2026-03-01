package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.*;
import com.franchiseproject.identityaccessservice.dto.response.*;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.service.RoleService;
import com.franchiseproject.identityaccessservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {
    UserService userService;
    RoleService roleService;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @GetMapping
    public ApiResponse<List<UserResponse>> getAll() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username :" + authentication.getName());
        authentication.getAuthorities().forEach(
                ga -> log.info(ga.getAuthority())
        );

        return ApiResponse.<List<UserResponse>>builder()
                .statusCode(200)
                .message("Get Data Success")
                .data(userService.getAll()
                        .stream()
                        .map(userMapper::toUserResponse)
                        .toList())
                .build();
    }

    @PostMapping("")
    public ApiResponse<UserCreationResponse> createUser(@RequestBody @Valid UserCreationRequest request, @AuthenticationPrincipal Jwt jwt) {

        Role role = roleService.getByName(request.getRoleName());

        if (role == null) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }

        String roleName = jwt.getClaimAsString("scope");
        System.out.println("ROLE: " + roleName);
        System.out.println("ROLE: " + role.getName());
        switch (role.getName()) {
            case "ADMIN":
                throw new AppException(ErrorCode.FORBIDDEN);
            case "MANAGER":
                if (!roleName.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "STAFF":
                if (!roleName.equals("MANAGER") && !roleName.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "CUSTOMER":
                if (!roleName.equals("STAFF") && !roleName.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        return ApiResponse.<UserCreationResponse>builder()
                .statusCode(201)
                .message("Created")
                .data(userService.createOne(user))
                .build();
    }

    /**
     * view account detail
     *
     * @param userId
     * @return
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID userId) {
        return ApiResponse.<UserResponse>builder()
                .statusCode(201)
                .message("Get One")
                .data(userMapper.toUserResponse(userService.getById(userId)))
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        return ApiResponse.<ChangePasswordResponse>builder()
                .statusCode(200)
                .message("Change password success")
                .data(ChangePasswordResponse.builder()
                        .isChange(userService.changePassword(request))
                        .build())
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> profile(@AuthenticationPrincipal Jwt jwt) {
        System.out.println(userService.getProfile(jwt.getSubject()));
        return ApiResponse.<UserResponse>builder()
                .statusCode(200)
                .message("View account detail")
                .data(userService.getProfile(jwt.getSubject()))
                .build();
    }

    @PutMapping("/update-account")
    public ApiResponse<UserUpdateResponse> updateAccountInfomation(@RequestBody UserUpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<UserUpdateResponse>builder()
                .statusCode(201)
                .message("Update account infomation successs")
                .data(userService.updateAccountInfomation(jwt.getSubject(), request))
                .build();
    }

    @DeleteMapping("/delete-account/{userId}")
    public ApiResponse<UserDeleteResponse> deleteAccountUser(@PathVariable UUID userId) {
        return ApiResponse.<UserDeleteResponse>builder()
                .statusCode(200)
                .message("Delete account usser id success")
                .data(userService.deleteAccountUser(userId))
                .build();
    }

    @PutMapping("/{userId}/assign-role")
    public ApiResponse<AssignRoleResponse> assignRole(
            @PathVariable UUID userId,
            @RequestBody AssignRoleRequest request,
            @AuthenticationPrincipal Jwt jwt ) {

        String assignerRole = jwt.getClaimAsString("scope");
        User user = userService.getById(userId);
        Role role = roleService.getByName(request.getRoleName());
        if (role == null || user == null) throw new AppException(ErrorCode.DATA_IS_NULL);

        switch (role.getName()) {
            case "ADMIN":
                throw new AppException(ErrorCode.FORBIDDEN);
            case "MANAGER":
                if (!assignerRole.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "STAFF":
                if (!assignerRole.equals("MANAGER") && !assignerRole.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "CUSTOMER":
                if (!assignerRole.equals("STAFF") && !assignerRole.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
        }

        return ApiResponse.<AssignRoleResponse>builder()
                .statusCode(200)
                .message("Assign role sucess")
                .data(userService.assignRole(role, user))
                .build();
    }

}
