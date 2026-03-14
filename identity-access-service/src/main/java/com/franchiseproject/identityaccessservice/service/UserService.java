package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.ChangePasswordRequest;
import com.franchiseproject.identityaccessservice.dto.request.SeachUsersRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserUpdateRequest;
import com.franchiseproject.identityaccessservice.dto.response.*;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
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
//    Page<User> search(
//            String keyword,
//            String role,
//            UserStatus status,
//            Boolean gender,
//            int page,
//            int size,
//            String sortBy,
//            String sortDir
//    );
    Page<User> search(SeachUsersRequest request);
    StatsCountUserResponse countUsers();
}
