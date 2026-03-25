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
public interface CustomerRepository extends JpaRepository<CustomerFranchise, UUID> {

    Page<CustomerFranchise> findAll(Pageable page);
    Page<CustomerFranchise> findByFranchiseIdAndStatus(UUID franchiseId, CustomerStatus status, Pageable pageable);

    Page<CustomerFranchise> findByFranchiseId(UUID franchiseId, Pageable pageable);

    Page<CustomerFranchise> findByStatus(CustomerStatus status, Pageable pageable);

    @Query("SELECT c FROM CustomerFranchise c " +
            "WHERE (:franchiseId IS NULL OR c.franchiseId = :franchiseId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:filterByCustomerIds = false OR c.customerId IN :customerIds)")
    Page<CustomerFranchise> searchCustomers(
            @Param("franchiseId") UUID franchiseId,
            @Param("status") CustomerStatus status,
            @Param("customerIds") List<UUID> customerIds,
            @Param("filterByCustomerIds") boolean filterByCustomerIds,
            Pageable pageable);

    boolean existsByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);

    Optional<CustomerFranchise> findByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);
}