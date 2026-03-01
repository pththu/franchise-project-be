package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.model.TierBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierBenefitRepository extends JpaRepository<TierBenefit, String> {
}