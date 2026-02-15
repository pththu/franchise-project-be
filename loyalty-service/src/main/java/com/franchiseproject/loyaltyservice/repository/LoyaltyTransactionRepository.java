package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {
    List<LoyaltyTransaction> findLoyaltyTransactionsByCustomerId(UUID customerId);
}
