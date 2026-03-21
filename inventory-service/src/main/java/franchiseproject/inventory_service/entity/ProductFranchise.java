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
@Table(name = "product_franchises")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFranchise {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "product_variant_id", columnDefinition = "UUID", nullable = false)
    UUID productVariantId;

    @Column(name = "franchise_id", columnDefinition = "UUID", nullable = false)
    UUID franchiseId;

    @Column(nullable = false)
    Integer quantity;
    @Column(nullable = false)
    Integer minStock;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    Instant updatedAt;
}
