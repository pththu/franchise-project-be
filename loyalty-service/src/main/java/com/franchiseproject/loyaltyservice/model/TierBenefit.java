package com.franchiseproject.loyaltyservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "tier_benefits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TierBenefit {

    @Id
    @Column(name = "tier_name", nullable = false, unique = true)
    String tierName;

    @Column(name = "required_points", nullable = false)
    Integer requiredPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tier_benefit_items", joinColumns = @JoinColumn(name = "tier_name"))
    @Column(name = "benefit", columnDefinition = "TEXT")
    List<String> benefits;
}