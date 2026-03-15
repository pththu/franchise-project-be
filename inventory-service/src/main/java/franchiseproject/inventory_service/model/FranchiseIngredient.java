package franchiseproject.inventory_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "franchise_ingredient")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseIngredient {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    UUID id;

    @Column(name = "product_id", nullable = false)
    UUID productId;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "unit")
    String unit;

    @Column(name = "min_stock")
    Integer minStock;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @Column(name = "product_name")
    String productName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "franchise_id", nullable = false)
    Franchise franchise;
}