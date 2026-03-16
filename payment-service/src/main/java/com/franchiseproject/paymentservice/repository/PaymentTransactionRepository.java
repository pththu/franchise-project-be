package com.franchiseproject.paymentservice.repository;

import com.franchiseproject.paymentservice.dto.response.PaymentTransactionResponse;
import com.franchiseproject.paymentservice.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    PaymentTransaction getById(UUID id);

    Optional<PaymentTransaction> findByOrderId(UUID orderId);

    List<PaymentTransaction> findByUserId(UUID userId);

    @Query("""
                SELECT p FROM PaymentTransaction p
                WHERE p.status = 'PENDING'
                AND p.createdAt < :timeout
            """)
    List<PaymentTransaction> findExpiredTransactions(LocalDateTime timeout);

}
