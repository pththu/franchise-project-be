package com.franchiseproject.identityaccessservice.repository;

import com.franchiseproject.identityaccessservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    boolean existsByApiAndHttpMethod(String api, String httpMethod);
}