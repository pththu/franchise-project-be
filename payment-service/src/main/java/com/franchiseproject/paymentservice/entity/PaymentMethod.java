package com.franchiseproject.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(name = "method_name")
    String methodName;
    @Column(name = "provider")
    String provider;
    @Column(name = "is_active")
    boolean active;

    @CreationTimestamp
    @Column(name= "created_at", updatable = false)
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "paymentMethod")
    List<UserPaymentMethod> userPaymentMethods;

    @OneToMany(mappedBy = "paymentMethod")
    List<PaymentTransaction> paymentMethods;

}
