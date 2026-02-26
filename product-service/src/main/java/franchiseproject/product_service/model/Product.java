package franchiseproject.product_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", unique = true, nullable = false)
    UUID id;

    @Column(name = "product_type", nullable = false)
    String productType;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    String description;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    BigDecimal price;

    @Column(name = "unit", nullable = false)
    String unit;

    @Column(name = "status", nullable = false)
    String status;

    @Column(name = "image_url", nullable = false, columnDefinition = "text")
    String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    // 🔥 Sửa LAZY → EAGER để tránh LazyInitializationException
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"products"})
    Category category;
}