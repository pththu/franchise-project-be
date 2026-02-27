package franchiseproject.product_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductDetailDTO(
        UUID id,
        String productType,
        String name,
        String description,
        BigDecimal price,
        String unit,
        String status,
        String imageUrl,
        UUID categoryId,
        String categoryName,
        Instant createdAt,
        Instant updatedAt
) {
}