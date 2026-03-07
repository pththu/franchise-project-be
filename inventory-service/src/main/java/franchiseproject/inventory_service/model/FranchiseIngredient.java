package franchiseproject.inventory_service.model;
import lombok.*;
import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "franchise_ingredient")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseIngredient {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true,nullable = false)
    UUID id;
    @Column(name = "product_id")
    UUID productId;
    @Column(name = "product_name")
    String productName;
    @Column(name = "quantity")
    Integer quantity;
    @Column(name = "unit")
    String unit;
    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
    @Column(name = "min_stock")
    Integer minStock;
    @ManyToOne()
    @JoinColumn(name = "franchise_id",columnDefinition = "UUID")
    Franchise franchise;
}
