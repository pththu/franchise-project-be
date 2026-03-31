package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import com.franchiseproject.loyaltyservice.model.LoyaltyWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyWalletRepository extends JpaRepository<LoyaltyWallet, UUID> {
    Optional<LoyaltyWallet> findByUserIdAndFranchiseId(UUID userId, UUID franchiseId);

    @Query("SELECT wallet.customerLoyaltyTier, COUNT(wallet) FROM LoyaltyWallet wallet GROUP BY wallet.customerLoyaltyTier")
    java.util.List<Object[]> countCustomersByTier();

    List<LoyaltyWallet> findByCustomerLoyaltyTier(CustomerLoyaltyTier tier);

    @Query("SELECT w FROM LoyaltyWallet w WHERE w.userId IN :userIds")
    List<LoyaltyWallet> findByUserIdIn(@Param("userIds") List<UUID> userIds);
}