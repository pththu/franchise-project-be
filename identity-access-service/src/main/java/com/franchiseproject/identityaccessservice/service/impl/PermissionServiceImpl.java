package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.dto.request.PermissionRequest;
import com.franchiseproject.identityaccessservice.dto.response.PermissionResponse;
import com.franchiseproject.identityaccessservice.entity.Permission;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.PermissionMapper;
import com.franchiseproject.identityaccessservice.repository.PermissionRepository;
import com.franchiseproject.identityaccessservice.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionResponse create(PermissionRequest request) {
        request.setApi(request.getApi().trim());
        request.setHttpMethod(request.getHttpMethod().trim().toUpperCase());

        if (permissionRepository.existsByApiAndHttpMethod(request.getApi(), request.getHttpMethod())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        Permission permission = permissionMapper.toPermission(request);
        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse update(UUID id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        request.setApi(request.getApi().trim());
        request.setHttpMethod(request.getHttpMethod().trim().toUpperCase());

        boolean isChanged = !permission.getApi().equals(request.getApi())
                || !permission.getHttpMethod().equals(request.getHttpMethod());

        if (isChanged && permissionRepository.existsByApiAndHttpMethod(request.getApi(), request.getHttpMethod())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }

        permissionMapper.updatePermission(permission, request);
        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    public void delete(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        permissionRepository.deleteById(id);
    }
}