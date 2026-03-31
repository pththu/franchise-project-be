package com.franchiseproject.loyaltyservice.model;

import com.franchiseproject.loyaltyservice.enums.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loyalty_transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyTransaction {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    @Column(name = "franchise_id", nullable = false)
    UUID franchiseId;
    @Column(name = "user_id", nullable = false)
    UUID userId;
    @Column(name = "order_id", nullable = false)
    UUID orderId;
    @Column(name = "promotion_id", nullable = true)
    UUID promotionId;
    @Column(nullable = false)
    int points;
    @Column(name = "balance_before", nullable = false)
    int balanceBefore;
    @Column(name = "balance_after", nullable = false)
    int balanceAfter;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    LoyaltyTransactionType type;
    @Column(name = "description", columnDefinition = "TEXT")
    String description;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
