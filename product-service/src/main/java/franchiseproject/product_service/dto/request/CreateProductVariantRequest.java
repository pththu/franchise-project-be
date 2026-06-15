package franchiseproject.product_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductVariantRequest {

    @NotBlank(message = "SKU không được để trống")
    String sku;

    @NotNull(message = "Giá không được null")
    @Min(value = 0, message = "Giá phải >= 0")
    BigDecimal price;

    @NotBlank(message = "Color không được để trống")
    String color; // hoặc enum nếu bạn đang dùng enum

    @NotBlank(message = "Size không được để trống")
    String size;

    @NotNull(message = "Stock không được null")
    @Min(value = 0, message = "Stock phải >= 0")
    Integer stock;

    // 🔥 QUAN TRỌNG NHẤT
    List<String> imageUrls;
}