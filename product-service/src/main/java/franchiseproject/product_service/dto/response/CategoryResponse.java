package franchiseproject.product_service.dto.response;

import franchiseproject.product_service.enums.CategoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    UUID id;
    String name;
    String description;
    CategoryStatus status;
    Instant createdAt;
    Instant lastUpdated;
    Integer productCount;
}