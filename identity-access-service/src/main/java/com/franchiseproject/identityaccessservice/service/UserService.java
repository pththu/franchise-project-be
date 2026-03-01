package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.*;
import com.franchiseproject.identityaccessservice.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getByUsername(String username);
    List<User> getAll ();
    UserResponse getById (UUID userId);
    UserCreationResponse createOne (User user);
    boolean changePassword(ChangePasswordRequest request);
    UserResponse getProfile(String username);
    UserUpdateResponse updateAccountInfomation(String username, UserUpdateRequest request);
    UserDeleteResponse deleteAccountUser(UUID userId);
    UserLockResponse lockUser(String username);
}
