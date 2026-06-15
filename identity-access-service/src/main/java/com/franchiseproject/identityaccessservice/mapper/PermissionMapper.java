package com.franchiseproject.identityaccessservice.mapper;

import com.franchiseproject.identityaccessservice.dto.request.PermissionRequest;
import com.franchiseproject.identityaccessservice.dto.response.PermissionResponse;
import com.franchiseproject.identityaccessservice.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);

    void updatePermission(@MappingTarget Permission permission, PermissionRequest request);
}