package franchiseproject.inventory_service.dto.response;

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
    UUID id;
    String size;
    String color;
    BigDecimal price;
    String imageUrl;
    
    UUID productId;
    String productName;
    String brand;
    String productType;
}
