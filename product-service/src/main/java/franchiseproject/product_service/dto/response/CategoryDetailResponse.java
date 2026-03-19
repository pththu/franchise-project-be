package franchiseproject.product_service.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailResponse {

    private UUID id;
    private String name;
    private String description;

    private List<ProductInCategoryResponse> product;
}
