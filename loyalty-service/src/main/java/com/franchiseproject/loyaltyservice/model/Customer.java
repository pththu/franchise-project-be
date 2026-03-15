package com.franchiseproject.loyaltyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.franchiseproject.loyaltyservice.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Customer {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    @Column(name = "full_name", nullable = false)
    String fullName;
    @Column(unique = true, nullable = false)
    String email;
    @Column(unique = true)
    String phone;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    CustomerStatus status;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
