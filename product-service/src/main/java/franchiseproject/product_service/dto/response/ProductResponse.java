package franchiseproject.product_service.dto.response;

import franchiseproject.product_service.enums.ProductStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    UUID id;
    String productType;
    String name;
    String unit;
    ProductStatus status;
    String branch;
    CategoryResponse category;
    Instant createdAt;
    Instant updatedAt;
    List<ProductVariantResponse> variants;
}
