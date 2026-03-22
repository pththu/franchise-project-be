package franchiseproject.product_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductVariantRequest {

    // ===== Identify (quan trọng nhất) =====
    UUID id; // null → create mới, có id → update


    // ===== Variant Info =====
    String sku;

    @Min(value = 0, message = "Giá phải >= 0")
    BigDecimal price;

    String color; // sẽ convert sang enum trong service

    String size;

    @Min(value = 0, message = "Stock phải >= 0")
    Integer stock;

    List<String> imageUrls;


    // ===== Status =====
    String status; // ACTIVE / INACTIVE
}