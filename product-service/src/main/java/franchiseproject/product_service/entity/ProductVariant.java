package franchiseproject.product_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductVariantStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    UUID id;
    // ✅ THÊM NGAY ĐÂY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ProductSize size;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ProductColor color;
    BigDecimal price;
    @Column(name = "sale_price")
    BigDecimal salePrice;
    int quantity;

    @Column(name = "image_url", columnDefinition = "TEXT")
    String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ProductVariantStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", columnDefinition = "UUID", nullable = false)
    @JsonIgnore
    Product product;
}
