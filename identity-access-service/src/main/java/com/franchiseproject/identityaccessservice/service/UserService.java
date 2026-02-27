package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.response.ChangePasswordResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<User> getAll ();
    UserResponse getOne (UUID userId);
    User createOne (UserCreationRequest request);
    boolean changePassword(ChangePasswordRequest request);
    UserResponse getProfile(String username);
}
