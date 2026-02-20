package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.model.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role createRole(Role role);

    Role updateRole(UUID id, Role role);

    List<Role> getAllRoles();

    void deleteRole(UUID id);
}
