package com.franchiseproject.loyaltyservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    String name;
    String description;
    @Column(name = "coupon_code")
    String couponCode;
    @Column(name = "discount_type")
    String discountType;
    @Column(name = "discount_value")
    BigDecimal discountValue;
    @Column(name = "usage_limit")
    Integer usageLimit;
    @Column(name = "coupon_usage_limit")
    Integer couponUsageLimit;
    @Column(name = "coupon_used_count")
    Integer couponUsedCount;
    @Column(name = "scope_type")
    String scopeType;
    @Column(name = "scope_value")
    String scopeValue;
    @Column(name = "start_time")
    LocalDateTime startTime;
    @Column(name = "end_time")
    LocalDateTime endTime;
    @Column(name = "created_at")
    LocalDateTime createdAt;
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    @Column(name = "points_to_redeem")
    Integer pointsToRedeem;
}
