package franchiseproject.product_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInCategoryResponse {

    private String name;
    private BigDecimal price;
}
