package com.franchiseproject.customerservice.repository;

import com.franchiseproject.customerservice.entity.CustomerFranchise;
import com.franchiseproject.customerservice.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerFranchiseRepository extends JpaRepository<CustomerFranchise, UUID> {

    Page<CustomerFranchise> findAll(Pageable page);

    Page<CustomerFranchise> findByFranchiseIdAndStatus(UUID franchiseId, CustomerStatus status, Pageable pageable);

    Page<CustomerFranchise> findByFranchiseId(UUID franchiseId, Pageable pageable);

    Page<CustomerFranchise> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM CustomerFranchise c " +
            "WHERE (:franchiseId IS NULL OR c.franchiseId = :franchiseId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:filterByUserIds = false OR c.userId IN :userIds)")
    Page<CustomerFranchise> searchCustomers(
            @Param("franchiseId") UUID franchiseId,
            @Param("status") CustomerStatus status,
            @Param("userIds") List<UUID> userIds,
            @Param("filterByUserIds") boolean filterByUserIds,
            Pageable pageable
    );

    boolean existsByUserIdAndFranchiseId(UUID userId, UUID franchiseId);

    @Query("""
        SELECT c FROM CustomerFranchise c
        WHERE c.franchiseId = :franchiseId
            AND c.userId = :userId
            AND c.status = 'ACTIVE'

    """)
    Optional<CustomerFranchise> findByUserIdAndFranchiseId(
            @Param("userId") UUID userId,
            @Param("franchiseId") UUID franchiseId);
}