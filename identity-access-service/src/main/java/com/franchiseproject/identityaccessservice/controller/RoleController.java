package com.franchiseproject.identityaccessservice.controller;

import com.franchiseproject.identityaccessservice.model.Role;
import com.franchiseproject.identityaccessservice.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RoleController {
    RoleService roleService;

    // Create Role
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    // Update Role
    @PutMapping("/{id}")
    public Role updateRole(@PathVariable UUID id,
                           @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    // Get all Roles
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    // Delete Role
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
    }
}