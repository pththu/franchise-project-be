package com.franchiseproject.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_payment_methods")
public class UserPaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(name = "user_id")
    UUID userId;
    @Column(name = "is_default")
    boolean isDefault;

    @CreationTimestamp
    @Column(name= "created_at", updatable = false)
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    PaymentMethod paymentMethod;
}
