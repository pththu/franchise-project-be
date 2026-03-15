package com.franchiseproject.identityaccessservice.repository;

import com.franchiseproject.identityaccessservice.entity.Role;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByName(String name);
    // Role findByName(String name);

    Optional<Role> findByName(String name);
}