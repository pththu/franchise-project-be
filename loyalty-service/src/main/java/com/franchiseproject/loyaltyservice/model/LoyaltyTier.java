package com.franchiseproject.loyaltyservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "loyalty_tiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyTier {

    @Id
    @Column(name = "tier_name", nullable = false, unique = true)
    String tierName;

    @Column(name = "required_points", nullable = false)
    Integer requiredPoints;
}