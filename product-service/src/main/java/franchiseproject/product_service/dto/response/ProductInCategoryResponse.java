package franchiseproject.product_service.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInCategoryResponse {

    private String name;
    private BigDecimal price;
}
