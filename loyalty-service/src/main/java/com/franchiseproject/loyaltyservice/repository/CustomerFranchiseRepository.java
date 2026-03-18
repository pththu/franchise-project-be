package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.enums.LoyaltyTier;
import com.franchiseproject.loyaltyservice.model.CustomerFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerFranchiseRepository extends JpaRepository<CustomerFranchise, UUID> {
    Optional<CustomerFranchise> findByCustomerIdAndFranchiseId(UUID customerId, UUID franchiseId);

    @Query("SELECT cf.loyaltyTier, COUNT(cf) FROM CustomerFranchise cf GROUP BY cf.loyaltyTier")
    java.util.List<Object[]> countCustomersByTier();

    List<CustomerFranchise> findByLoyaltyTier(LoyaltyTier tier);
}