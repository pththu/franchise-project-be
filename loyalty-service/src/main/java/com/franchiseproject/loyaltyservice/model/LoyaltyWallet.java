package com.franchiseproject.loyaltyservice.model;

import com.franchiseproject.loyaltyservice.enums.CustomerLoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loyalty_wallets")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyWallet {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;

    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    UUID userId;

    @Column(name = "franchise_id", columnDefinition = "UUID", nullable = false)
    UUID franchiseId;

    @Enumerated(EnumType.STRING)
    CustomerLoyaltyTier customerLoyaltyTier;

    @Column(name = "loyalty_current_point")
    int loyaltyCurrentPoint;

    @Column(name = "loyalty_total_point")
    int loyaltyTotalPoint;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}