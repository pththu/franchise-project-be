package franchiseproject.product_service.dto.response;

import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {
    UUID id;
    ProductSize size;
    ProductColor color;
    BigDecimal price;
    int quantity;
    String imageUrl;
}
