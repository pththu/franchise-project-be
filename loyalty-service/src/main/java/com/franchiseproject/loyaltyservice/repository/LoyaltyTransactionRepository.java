package com.franchiseproject.loyaltyservice.repository;

import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import com.franchiseproject.loyaltyservice.model.LoyaltyTransaction;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {
    List<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    @Query("SELECT COALESCE(SUM(lt.points), 0) FROM LoyaltyTransaction lt WHERE lt.type = :type")
    Long sumPointsByType(@Param("type") LoyaltyTransactionType type);

    @Query("SELECT COUNT(lt) FROM LoyaltyTransaction lt WHERE lt.type = :type")
    Long countTransactionsByType(@Param("type") LoyaltyTransactionType type);

    boolean existsByCustomerIdAndPromotionIdAndType(UUID customerId, UUID promotionId, LoyaltyTransactionType type);
}