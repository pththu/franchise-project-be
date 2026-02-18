package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.dto.ApiResponse;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {
    UserService userService;

    @GetMapping
    public ApiResponse<List<User>> getAll() {
        List<User> users = userService.getAll();
        ApiResponse<List<User>> response = ApiResponse.<List<User>>builder()
                .statusCode(200)
                .message("Get Data Success")
                .data(users)
                .build();

        return response;
    }

    @PostMapping("")
    public ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<User>builder()
                .statusCode(201)
                .message("Created")
                .data(userService.createOne(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<User>  getUserById(@PathVariable("id") UUID userId) {
        return ApiResponse.<User>builder()
                .statusCode(201)
                .message("Get One")
                .data(userService.getOne(userId))
                .build();
    }
}
