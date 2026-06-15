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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_stock_id", nullable = false)
    ProductStock productStock;

    @Column(name = "change_quantity")
    Integer changeQuantity;

    @Column(name = "before_quantity")
    Integer beforeQuantity;

    @Column(name = "after_quantity")
    Integer afterQuantity;

    // Type: SALE, IMPORT, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT, RETURN
    @Column(name = "type", nullable = false, length = 30)
    String type;

    @Column(name = "status", length = 20)
    String status;

    @Column(name = "reference_id")
    UUID referenceId; // ID of Order, StockTransfer, or StockRequest

    @Column(name = "reference_type")
    String referenceType; // "ORDER", "TRANSFER", "REQUEST", "IMPORT"

    @Column(name = "created_by")
    UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}