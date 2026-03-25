package com.franchiseproject.customerservice.entity;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.enums.CustomerType;
import com.franchiseproject.customerservice.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
    @Column(name = "customer_id", columnDefinition = "UUID", nullable = false)
    UUID customerId;
    @Column(name = "franchise_id")
    UUID franchiseId;
    @Enumerated(EnumType.STRING)
    LoyaltyTier loyaltyTier;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    CustomerType type;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    CustomerStatus status;
    @Column(name = "loyalty_current_point")
    Integer loyaltyCurrentPoint;
    @Column(name = "loyalty_total_point")
    Integer loyaltyTotalPoint;
    @Column(name = "first_order_at")
    Instant firstOrderAt;
    @Column(name = "last_order_at")
    Instant lastOrderAt;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
