package com.franchiseproject.customerservice.model;

import com.franchiseproject.customerservice.enums.CustomerFranchiseStatus;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_franchise")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerFranchise {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    @Column(name = "franchise_id")
    UUID franchiseId;
    @Enumerated(EnumType.STRING)
    LoyaltyTier loyaltyTier;
    @Column(name = "loyalty_current_point")
    int loyaltyCurrentPoint;
    @Column(name = "loyalty_total_point")
    int loyaltyTotalPoint;
    @Column(name = "first_order_at")
    Instant firstOrderAt;
    @Column(name = "last_order_at")
    Instant lastOrderAt;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", columnDefinition = "UUID")
    Customer customer;
}
