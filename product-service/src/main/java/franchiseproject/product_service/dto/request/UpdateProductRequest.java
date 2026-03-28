package franchiseproject.product_service.dto.request;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductRequest {

    // ===== Product Info =====
    String name;
    String description;
    UUID categoryId;
    String productType;
    String unit;
    String brand;
    String status; // ACTIVE / INACTIVE

    // ===== Variants =====
    @Valid
    List<UpdateProductVariantRequest> variants;
}