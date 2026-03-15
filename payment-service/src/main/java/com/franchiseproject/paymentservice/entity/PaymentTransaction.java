package com.franchiseproject.paymentservice.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.franchiseproject.paymentservice.enums.StatusTransaction;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Entity
    @Table(name="payment_transactions")
    public class PaymentTransaction {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        UUID id;
        @Column(name = "user_id", nullable = false)
        UUID userId;
        @Column(name = "order_id", nullable = false, unique = true)
        UUID orderId;
        @Column(name = "amount", nullable = false)
        BigDecimal amount;
        @Enumerated(EnumType.STRING)
        @Column(name = "status_transaction",  nullable = false)
        StatusTransaction status;
        @Column(name="transaction_ref", unique = true)
        String transactionRef;

        @CreationTimestamp
        @Column(name = "create_at", updatable = false)
        LocalDateTime createdAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payment_method_id", nullable = false)
        @JsonManagedReference
        PaymentMethod paymentMethod;

        @OneToMany(mappedBy = "paymentTransaction")
        List<Refund> refunds;

    }
