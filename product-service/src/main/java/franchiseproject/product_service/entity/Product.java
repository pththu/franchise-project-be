package franchiseproject.product_service.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import franchiseproject.product_service.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
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
    @Column(name = "unit", nullable = false)
    String unit;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    ProductStatus status;
    @Column(name = "brand", nullable = false)
    String brand;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("product")
    Category category;
    @Column(name = "name_en")
    String nameEn;

    @Column(name = "name_ja")
    String nameJa;

    @Column(name = "description_en")
    String descriptionEn;

    @Column(name = "description_ja")
    String descriptionJa;

    @Column(name = "brand_en")
    String brandEn;

    @Column(name = "brand_ja")
    String brandJa;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductVariant> variants;
}