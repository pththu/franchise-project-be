package com.franchiseproject.customerservice.repository;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN c.customerFranchises cf " +
            "WHERE (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
            @Param("keyword") String keyword,
            @Param("status") CustomerStatus status,
            Pageable pageable
    );
}