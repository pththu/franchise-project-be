package franchiseproject.inventory_service.model;

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
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransaction {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "franchise_ingredient_id", nullable = false)
    UUID franchiseIngredientId;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "before_quantity")
    Integer beforeQuantity;

    @Column(name = "after_quantity")
    Integer afterQuantity;

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

    @Column(name = "staff_id")
    UUID staffId;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_ingredient_id", referencedColumnName = "id", insertable = false, updatable = false)
    FranchiseIngredient franchiseIngredient;
}