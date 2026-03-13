package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.*;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getByUsername(String username);
    List<User> getAll ();
    User getById (UUID userId);
    UserCreationResponse createOne (User user);
    boolean changePassword(ChangePasswordRequest request);
    UserResponse getProfile(UUID userId);
    UserUpdateResponse updateAccountInfomation(String username, UserUpdateRequest request);
    UserDeleteResponse deleteAccountUser(UUID userId);
    AssignRoleResponse assignRole(Role role, User user);
    Page<User> getAll(int page);
    Page<User> search(String keyword, int page);
}
