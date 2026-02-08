package com.franchiseproject.customerservice.repository;

import com.franchiseproject.customerservice.model.CustomerFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerFranchiseRepository extends JpaRepository<CustomerFranchise, UUID> {
}
