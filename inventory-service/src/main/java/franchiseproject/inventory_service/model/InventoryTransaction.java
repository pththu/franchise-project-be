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
    @Column(unique = true,nullable = false)
    UUID id;
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
    @Column(name = "alert_trigger_at")
    Instant alertTriggeredAt;
    @Column(name = "status")
    String status;
    @Column(name = "staff_id")
    UUID staffId;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
    @UpdateTimestamp
    @Column(name = "update_at")
    Instant updateAt;
    @ManyToOne()
    @JoinColumn(name = "franchise_ingredient_id", columnDefinition = "UUID", insertable = false, updatable = false)
    FranchiseIngredient franchiseIngredient;
}
