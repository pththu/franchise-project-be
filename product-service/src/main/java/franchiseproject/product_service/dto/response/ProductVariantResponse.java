package franchiseproject.product_service.dto.response;

import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductVariantStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    UUID id;
    ProductSize size;
    ProductColor color;
    BigDecimal price;
    ProductVariantStatus status;
    int quantity;
    List<String> imageUrls;
}
