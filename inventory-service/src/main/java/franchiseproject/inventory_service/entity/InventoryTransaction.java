package franchiseproject.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransaction {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "before_quantity")
    Integer beforeQuantity;

    @Column(name = "after_quantity")
    Integer afterQuantity;

    // + || -
    @Column(name = "type")
    String type;

    @Column(name = "threshold")
    Integer threshold;

    @Column(name = "alert_triggered_at")
    Instant alertTriggeredAt;

    @Column(name = "without_threshold")
    Boolean withoutThreshold;

    @Column(name = "status")
    String status;

    @Column(name = "created_by")
    UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_franchise_id", columnDefinition = "UUID", nullable = false)
    ProductFranchise productFranchise;
}