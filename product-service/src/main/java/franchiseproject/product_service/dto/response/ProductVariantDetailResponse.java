package franchiseproject.product_service.dto.response;
import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantDetailResponse {
    UUID id; // Variant ID
    ProductSize size;
    ProductColor color;
    BigDecimal price;
    String imageUrl;
    
    // Parent Product Information
    UUID productId;
    String productName;
    String brand;
    String productType;
}
