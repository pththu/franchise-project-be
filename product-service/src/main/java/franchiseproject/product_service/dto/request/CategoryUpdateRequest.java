package franchiseproject.product_service.dto.request;

import franchiseproject.product_service.enums.CategoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    String name;
    String description;
    String status;
}
