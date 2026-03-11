package com.franchiseproject.deliveryservice.model;

import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "delivery_history")
public class DeliveryHistory {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "history_id",unique = true, nullable = false)
    UUID historyId;
    @Enumerated(EnumType.STRING)
    DeliverySatus status;
    String note;
    @Column(name = "updated_by")
    UUID updatedBy;
    Instant receivedAt;
    @CreationTimestamp
    @Column(name="updated_at")
    Instant updatedAt;
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    Delivery delivery;

}
