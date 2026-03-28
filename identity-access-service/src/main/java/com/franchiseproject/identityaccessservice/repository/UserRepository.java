package com.franchiseproject.identityaccessservice.repository;

import com.franchiseproject.identityaccessservice.entity.User;
import com.franchiseproject.identityaccessservice.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByRoleName(String roleName);

    Page<User> findAll(Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.role.name) LIKE LOWER(CAST(:roleName AS string))
            AND u.status = 'ACTIVE'
    """)
    Page<User> findByRole(@Param("roleName") String roleName, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL OR 
                LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR
                LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR
                LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR
                LOWER(u.phone) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))
            AND (:roleName IS NULL OR u.role.name = CAST(:roleName AS string))
            AND (:status IS NULL OR u.status = :status)
            AND (:gender IS NULL OR u.gender = :gender)
        """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleName") String roleName,
            @Param("status") UserStatus status,
            @Param("gender") Boolean gender,
            Pageable pageable);

    @Query("""
            SELECT 
            COUNT(u),
            SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.status = 'SUSPENDED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.status = 'DELETED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.role.name = 'ADMIN' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.role.name = 'MANAGER' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.role.name = 'STAFF' THEN 1 ELSE 0 END),
            SUM(CASE WHEN u.role.name = 'CUSTOMER' THEN 1 ELSE 0 END)
            FROM User u
            """)
    Object[] countUserStats();

}
