package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.ChangePasswordResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserDeleteResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserUpdateResponse;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.UserMapper;
import com.franchiseproject.identityaccessservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    UserMapper userMapper;

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
    public ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<User>builder()
                .statusCode(201)
                .message("Created")
                .data(userService.createOne(request))
                .build();
    }

    /**
     * view account detail
     * @param userId
     * @return
     */
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID userId) {
        return ApiResponse.<UserResponse>builder()
                .statusCode(201)
                .message("Get One")
                .data(userService.getById(userId))
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
}
