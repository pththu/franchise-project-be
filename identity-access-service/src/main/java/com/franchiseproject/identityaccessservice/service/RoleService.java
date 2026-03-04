package com.franchiseproject.identityaccessservice.service;

import com.franchiseproject.identityaccessservice.dto.request.RoleCreationRequest;
import com.franchiseproject.identityaccessservice.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role createRole(RoleCreationRequest role);

    Role updateRole(UUID id, RoleCreationRequest role);

    Role getById(UUID id);

    Role getByName(String name);

    List<Role> getAll();

    boolean deleteRole(UUID id);
}

