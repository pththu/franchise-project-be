package franchiseproject.product_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    String name;

    String description;
    String brand;

    @NotNull(message = "Category không được null")
    UUID categoryId;

    @Valid
    @NotNull(message = "Danh sách variant không được null")
    List<CreateProductVariantRequest> variants;
}