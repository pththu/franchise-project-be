package com.franchiseproject.loyaltyservice.model;

import com.franchiseproject.loyaltyservice.enums.LoyalyTransactionType;
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
    @Column(name = "franchise_id", nullable = true)
    UUID franchiseId;
    @Column(name = "customer_id", nullable = false)
    UUID customerId;
    @Column(name = "promotion_id", nullable = false)
    UUID promotionId;
    int points;
    @Column(name = "balance_before")
    int balanceBefore;
    @Column(name = "balance_after")
    int balanceAfter;
    @Enumerated(EnumType.STRING)
    LoyalyTransactionType type;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
