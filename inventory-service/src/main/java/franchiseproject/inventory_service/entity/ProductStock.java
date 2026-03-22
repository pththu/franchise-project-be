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
@Table(name = "product_stocks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductStock {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "product_variant_id", nullable = false)
    UUID productVariantId;

    @Column(name = "location_id", nullable = false)
    Long locationId; // TODO: Chuyển lại thành UUID sau khi team Franchise sửa đổi

    @Column(name = "location_type", nullable = false, length = 20)
    @Builder.Default
    String locationType = "FRANCHISE"; // "FRANCHISE", "WAREHOUSE"

    @Column(nullable = false)
    @Builder.Default
    Integer quantity = 0; // Actual physical stock available for sale

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    Integer reservedQuantity = 0; // Stock reserved for pending orders

    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    Integer minStock = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
