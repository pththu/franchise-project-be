package com.franchiseproject.orderservice.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_log")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusLog {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false)
    UUID id;
    @Column(name = "status_id", nullable = false)
    UUID statusId;
    @Column(name = "form_status", nullable = false)
    String formStatus;
    @Column(name = "to_status", nullable = false)
    String toStatus;
    @Column(name = "notes_log", nullable = false)
    String noteLog;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant  createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",columnDefinition = "UUID")
    Order ordrer;
}
