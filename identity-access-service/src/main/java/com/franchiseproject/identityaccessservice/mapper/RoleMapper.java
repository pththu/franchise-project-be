package com.franchiseproject.identityaccessservice.mapper;

import com.franchiseproject.identityaccessservice.dto.request.RoleCreationRequest;
import com.franchiseproject.identityaccessservice.dto.response.RoleResponse;
import com.franchiseproject.identityaccessservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);
}
