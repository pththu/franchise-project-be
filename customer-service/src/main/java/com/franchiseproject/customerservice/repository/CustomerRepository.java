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
//    @Query("SELECT DISTINCT c FROM Customer c " +
//            "LEFT JOIN c.customerFranchises cf " +
//            "WHERE (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//            "AND (:status IS NULL OR c.status = :status)")
//    Page<Customer> searchCustomers(
//            @Param("keyword") String keyword,
//            @Param("status") CustomerStatus status,
//            Pageable pageable
//    );
//
//    @Query("SELECT DISTINCT c FROM Customer c " +
//            "WHERE (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//            "AND (:status IS NULL OR c.status = :status)")
//    Page<Customer> searchAllCustomers(
//            @Param("keyword") String keyword,
//            @Param("status") CustomerStatus status,
//            Pageable pageable
//    );
//
//    @Query("SELECT DISTINCT c FROM Customer c " +
//            "INNER JOIN c.customerFranchises cf " +
//            "WHERE cf.franchiseId = :franchiseId " +
//            "AND (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
//            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
//            "AND (:status IS NULL OR c.status = :status)")
//    Page<Customer> searchCustomersByFranchise(
//            @Param("franchiseId") UUID franchiseId,
//            @Param("keyword") String keyword,
//            @Param("status") CustomerStatus status,
//            Pageable pageable
//    );
//
//    Optional<Customer> findByPhoneOrEmail(String phone, String email);
//
//    Optional<Customer> findByPhone(String phone);
//
//    Optional<Customer> findByEmail(String email);
//
//    List<CustomerFranchise> findByCustomerId(UUID customerId);
//
//    boolean existsByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);

    Page<CustomerFranchise> findByFranchiseIdAndStatus(UUID franchiseId, CustomerStatus status, Pageable pageable);

    Page<CustomerFranchise> findByFranchiseId(UUID franchiseId, Pageable pageable);

    Page<CustomerFranchise> findByStatus(CustomerStatus status, Pageable pageable);

    boolean existsByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);

    Optional<CustomerFranchise> findByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);
}