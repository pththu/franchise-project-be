package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.model.LoyaltyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyRuleRepository extends JpaRepository<LoyaltyRule, Long> {
}