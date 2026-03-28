package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.client.FranchiseClient;
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
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth/users")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    UserService userService;
    RoleService roleService;
    UserMapper userMapper;

    @GetMapping
    public ApiResponse<Page<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(ga -> log.info(ga.getAuthority()));

        Page<UserResponse> data = userService.getAll(page).map(userMapper::toUserResponse);

        return ApiResponse.<Page<UserResponse>>builder()
                .statusCode(200)
                .message("Get Data Success")
                .data(data)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<UserResponse>> search(
            @Valid @ModelAttribute SeachUsersRequest request) {

        log.info("Search users API called with request: {}", request);
        Page<UserResponse> data = userService.search(request);

        log.info("data: {}", data.getContent().get(0).getFranchise().getName());

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .statusCode(200)
                .message("Search users success")
                .data(PageResponse.<UserResponse>builder()
                        .content(data.getContent())
                        .page(data.getNumber())
                        .size(data.getSize())
                        .totalElements(data.getTotalElements())
                        .totalPages(data.getTotalPages())
                        .build())
                .build();
    }

    @GetMapping("/counts")
    public ApiResponse<StatsCountUserResponse> getCountUsers() {
        return ApiResponse.<StatsCountUserResponse>builder()
                .statusCode(200)
                .message("Get count user in system")
                .data(userService.countUsers())
                .build();
    }

    /**
     * Admin/Manager tạo user thủ công với role cụ thể.
     * Sau khi tạo, user được add ngay vào Cognito group theo roleName.
     */
    @PostMapping
    public ApiResponse<UserCreationResponse> createUser(
            @RequestBody @Valid UserCreationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Role role = roleService.getByName(request.getRoleName());
        if (role == null) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }

        String callerRole = jwt.getClaimAsString("role");
        log.info("CreateUser: callerRole={}, targetRole={}", callerRole, role.getName());

        switch (role.getName()) {
            case "ADMIN":
                throw new AppException(ErrorCode.FORBIDDEN);
            case "MANAGER":
                if (!callerRole.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "STAFF":
                if (!callerRole.equals("MANAGER") && !callerRole.equals("ADMIN"))
                    throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "CUSTOMER":
                if (!callerRole.equals("STAFF") && !callerRole.equals("ADMIN"))
                    throw new AppException(ErrorCode.FORBIDDEN);
                break;
        }

        return ApiResponse.<UserCreationResponse>builder()
                .statusCode(201)
                .message("Created")
                .data(userService.createOne(request, role))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID userId) {
        User user = userService.getById(userId);
        log.info("getUserById: userId={}, verifyEmail={}", userId, user.isVerifyEmail());
        return ApiResponse.<UserResponse>builder()
                .statusCode(200)
                .message("Get One")
                .data(userMapper.toUserResponse(user))
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<UserResponse> profile(@AuthenticationPrincipal Jwt jwt) {
        log.info("profile: subject={}", jwt.getSubject());
        return ApiResponse.<UserResponse>builder()
                .statusCode(200)
                .message("View account detail")
                .data(userService.getProfile(UUID.fromString(jwt.getSubject())))
                .build();
    }

    @PostMapping("/bulk")
    public ApiResponse<List<UserResponse>> getUsersByIds(@RequestBody List<UUID> ids) {
        log.info("Bulk fetching users for IDs: {}", ids);
        List<UserResponse> data = userService.getUsersByIds(ids).stream()
                .map(userMapper::toUserResponse)
                .toList();

        return ApiResponse.<List<UserResponse>>builder()
                .statusCode(200)
                .message("Bulk fetch success")
                .data(data)
                .build();
    }

    @PutMapping("/{userId}/update")
    public ApiResponse<UserUpdateResponse> updateUser(
            @PathVariable("userId") UUID targetId,
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String callerRole = jwt.getClaimAsString("role");

        if (!callerRole.equals("ADMIN")
                && !callerRole.equals("STAFF")
                && !callerRole.equals("STORE_MANAGER")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        return ApiResponse.<UserUpdateResponse>builder()
                .statusCode(200)
                .message("Update account information success")
                .data(userService.updateAccountInformation(targetId, request))
                .build();
    }

    @PutMapping("/update-profile")
    public ApiResponse<UserUpdateResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        if (request == null && request.getFullName() == null && request.getGender() == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        if (jwt.getSubject() == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        UUID userId = UUID.fromString(jwt.getSubject());

        return ApiResponse.<UserUpdateResponse>builder()
                .statusCode(200)
                .message("Update profile success")
                .data(userService.updateProfile(userId, request))
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.<ChangePasswordResponse>builder()
                .statusCode(200)
                .message("Change password success")
                .data(ChangePasswordResponse.builder()
                        .isChange(userService.changePassword(request, userId))
                        .build())
                .build();
    }

    /**
     * Update trạng thái user: ACTIVE | SUSPENDED | DELETED
     * Body: { "status": "SUSPENDED" }
     *
     */
    @PutMapping("/{userId}/status")
    public ApiResponse<UserStatusResponse> updateStatus(
            @PathVariable UUID userId,
            @RequestParam UserStatus status,
            @AuthenticationPrincipal Jwt jwt) {

        String callerRole = jwt.getClaimAsString("role");

        // Chỉ ADMIN, STORE_MANAGER và STAFF được phép thay đổi status
        if (!callerRole.equals("ADMIN")
                && !callerRole.equals("STAFF")
                && !callerRole.equals("STORE_MANAGER")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // STORE MANAGER chỉ có thể suspend/activate STAFF và CUSTOMER, không được động vào ADMIN/MANAGER khác
        if (callerRole.equals("STORE_MANAGER")) {
            User target = userService.getById(userId);
            String targetRole = target.getRole().getName();
            if (targetRole.equals("ADMIN")
                    || targetRole.equals("MANAGER")
                    || targetRole.equals("STORE_MANAGER")) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        }

        return ApiResponse.<UserStatusResponse>builder()
                .statusCode(200)
                .message("Update user status success")
                .data(userService.updateStatus(userId, status))
                .build();
    }

    /**
     * Assign role mới cho user.
     * Tự động: xóa khỏi Cognito group cũ → add vào Cognito group mới → update DB.
     */
    @PutMapping("/{userId}/assign-role")
    public ApiResponse<AssignRoleResponse> assignRole(
            @PathVariable UUID userId,
            @PathParam("roleName") String roleName,
            @AuthenticationPrincipal Jwt jwt) {

        String assignerRole = jwt.getClaimAsString("role");
        log.info("AssignRole: assigner={}, targetUser={}, newRole={}", assignerRole, userId, roleName);

        User user = userService.getById(userId);
        Role role = roleService.getByName(roleName);
        if (role == null || user == null) throw new AppException(ErrorCode.NOT_FOUND);

        switch (role.getName()) {
            case "ADMIN":
            case "CUSTOMER":
                throw new AppException(ErrorCode.FORBIDDEN);
            case "MANAGER":
                if (!assignerRole.equals("ADMIN")) throw new AppException(ErrorCode.FORBIDDEN);
                break;
            case "STAFF":
                if (!assignerRole.equals("MANAGER") && !assignerRole.equals("ADMIN"))
                    throw new AppException(ErrorCode.FORBIDDEN);
                break;
        }

        return ApiResponse.<AssignRoleResponse>builder()
                .statusCode(200)
                .message("Assign role success")
                .data(userService.assignRole(role, user))
                .build();
    }

    @PutMapping("/delete-account/{userId}")
    public ApiResponse<UserDeleteResponse> deleteAccountUser(@PathVariable UUID userId) {
        return ApiResponse.<UserDeleteResponse>builder()
                .statusCode(200)
                .message("Delete account user success")
                .data(userService.deleteAccountUser(userId))
                .build();
    }

    // uncheck
    @GetMapping("/franchise/staff")
    public ApiResponse<Page<UserResponse>> getStaffByFranchise(
            @PathParam("frannchiseId") UUID franchiseId,
            @PathParam("page") int page
    ) {
        return ApiResponse.<Page<UserResponse>>builder()
                .statusCode(200)
                .message("Get list staff")
                .data(userService.getStaffByFranchise(franchiseId, page))
                .build();
    }


//    @GetMapping("/api/auth/internal/users/{userId}")
//    public ApiResponse<UserResponse> getUserInternal(@PathVariable UUID userId) {
//        return ApiResponse.<UserResponse>builder()
//                .statusCode(200)
//                .message("Get user successfully")
//                .data(userService.getUserById(userId))
//                .build();
//    }

//    @PostMapping("/api/auth/internal/users/search-by-ids")
//    public ApiResponse<List<UserResponse>> getUsersByIdsInternal(@RequestBody List<UUID> userIds) {
//        var users = userService.getUsersByIds(userIds);
//
//        List<UserResponse> userResponses = users.stream()
//                .map(userMapper::toUserResponse)
//                .toList();
//
//        return ApiResponse.<List<UserResponse>>builder()
//                .statusCode(200)
//                .message("Get users successfully")
//                .data(userResponses)
//                .build();
//    }
}
