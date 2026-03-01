package com.franchiseproject.loyaltyservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "loyalty_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyRule {
    @Id
    @Column(name = "id")
    Long id;

    @Column(name = "amount_per_point", nullable = false)
    Double amountPerPoint;
}