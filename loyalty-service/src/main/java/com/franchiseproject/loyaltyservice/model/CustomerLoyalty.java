package com.franchiseproject.loyaltyservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_loyalty")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerLoyalty {
    @Id
    @Column(name = "customer_id")
    UUID customerId; // Sử dụng customer_id làm PK
    @Column(name = "tier_id")
    UUID tierId; // Chỉ lưu ID, không ràng buộc FK mapping
    @Column(name = "total_points")
    Integer totalPoints;
    @Column(name = "current_points")
    Integer currentPoints;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}