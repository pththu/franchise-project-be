package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.PermissionRequest;
import com.franchiseproject.identityaccessservice.dto.response.PermissionResponse;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> getAll();

    PermissionResponse update(UUID id, PermissionRequest request);

    void delete(UUID id);
}