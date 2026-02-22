package com.franchiseproject.identityaccessservice.mapper;

import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "roleId")
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isVerifyEmail", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "franchiseId", ignore = true)
    @Mapping(target = "isVerifyEmail", ignore = true)
    User toUser(CustomerRegisterRequest request);

    UserResponse toUserResponse(User user);

    default Role map(UUID roleId) {
        if (roleId == null) return null;
        Role role = new Role();
        role.setId(roleId);
        return role;
    }
}
