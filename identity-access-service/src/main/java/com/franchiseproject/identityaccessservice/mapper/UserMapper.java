package com.franchiseproject.identityaccessservice.mapper;

import com.franchiseproject.identityaccessservice.client.FranchiseClient;
import com.franchiseproject.identityaccessservice.dto.request.CustomerRegisterRequest;
import com.franchiseproject.identityaccessservice.dto.request.RoleCreationRequest;
import com.franchiseproject.identityaccessservice.dto.request.UserCreationRequest;
import com.franchiseproject.identityaccessservice.dto.response.FranchiseResponse;
import com.franchiseproject.identityaccessservice.dto.response.RoleResponse;
import com.franchiseproject.identityaccessservice.dto.response.UserResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "verifyEmail", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "franchiseId", ignore = true)
    @Mapping(target = "verifyEmail", ignore = true)
    User toUser(CustomerRegisterRequest request);

    @Mapping(target = "verifyEmail", source = "verifyEmail")
    @Mapping(target = "franchise", source = "franchiseId")
    UserResponse toUserResponse(User user, @Context FranchiseClient franchiseClient);

    default FranchiseResponse mapFranchise(
            UUID franchiseId,
            @Context FranchiseClient franchiseClient) {
        if (franchiseId == null) {
            System.out.println("return null: "+ franchiseId);
            return null;
        };
        return franchiseClient.getFranchiseById(franchiseId);
    }
}
