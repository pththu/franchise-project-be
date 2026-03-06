package com.franchiseproject.identityaccessservice.service.impl;

import com.franchiseproject.identityaccessservice.dto.request.RoleCreationRequest;
import com.franchiseproject.identityaccessservice.entity.Role;
import com.franchiseproject.identityaccessservice.exception.AppException;
import com.franchiseproject.identityaccessservice.exception.ErrorCode;
import com.franchiseproject.identityaccessservice.mapper.RoleMapper;
import com.franchiseproject.identityaccessservice.repository.PermissionRepository;
import com.franchiseproject.identityaccessservice.repository.RoleRepository;
import com.franchiseproject.identityaccessservice.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(RoleCreationRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        return roleRepository.save(roleMapper.toRole(request));
    }

    @Override
    public Role updateRole(UUID id, RoleCreationRequest role) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());

        return roleRepository.save(existingRole);
    }

    @Override
    public Role getById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    @Override
    public Role getByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Cannot find Role ADMINl"));
    }

    @Override
    public boolean deleteRole(UUID id) {
        if (roleRepository.existsById(id)) {
            new AppException(ErrorCode.NOT_FOUND);
        }
        roleRepository.deleteById(id);
        return true;
    }

    @Override
    public Role assignPermissions(UUID roleId, com.franchiseproject.identityaccessservice.dto.request.RolePermissionRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        List<com.franchiseproject.identityaccessservice.entity.Permission> permissions =
                permissionRepository.findAllById(request.getPermissionIds());

        role.setPermissions(new java.util.HashSet<>(permissions));

        return roleRepository.save(role);
    }
}