package com.franchiseproject.identityaccessservice.repository;

import com.franchiseproject.identityaccessservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

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
