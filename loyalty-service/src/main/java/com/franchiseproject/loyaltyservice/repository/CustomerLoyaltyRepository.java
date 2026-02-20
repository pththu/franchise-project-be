package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.model.CustomerLoyalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerLoyaltyRepository extends JpaRepository<CustomerLoyalty, UUID> {}
