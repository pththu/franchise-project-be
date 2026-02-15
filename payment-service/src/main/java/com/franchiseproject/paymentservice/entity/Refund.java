package com.franchiseproject.paymentservice.entity;

import com.franchiseproject.paymentservice.enums.StatusRefund;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name="refunds")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(name = "requested_by", nullable = false)
    UUID requestedBy;
    @Column(name = "refund_amount",  nullable = false)
    BigDecimal refundAmount;
    @Column(name = "reason")
    String reason;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    StatusRefund status;

    @CreationTimestamp
    @Column(name= "created_at", updatable = false)
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    PaymentTransaction paymentTransaction;
}
