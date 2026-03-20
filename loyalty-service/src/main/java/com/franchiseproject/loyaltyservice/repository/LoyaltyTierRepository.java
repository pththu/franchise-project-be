package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.model.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, String> {
}