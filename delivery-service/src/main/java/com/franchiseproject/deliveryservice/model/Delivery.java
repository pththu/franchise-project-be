package com.franchiseproject.deliveryservice.model;

import com.franchiseproject.deliveryservice.enums.DeliverySatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name="delivery")
public class Delivery {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name="delivery_id")
    UUID deliveryId;
    @Column(name="order_id")
    UUID orderId;
    @Column(name="shipper_id")
    UUID shipperId;
    @Column(name="weight")
    double weight;
    @Column(name="scheduled_at")
    Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    DeliverySatus status;

    @CreationTimestamp
    @Column(name="created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name="update_at")
    Instant updatedAt;

    @OneToMany(mappedBy = "delivery")
    List<DeliveryHistory> deliveryHistory;
}
