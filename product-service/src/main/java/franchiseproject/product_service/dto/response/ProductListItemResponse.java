package franchiseproject.product_service.dto.response;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductListItemResponse(
        UUID id,
        String productType,
        String name,
        BigDecimal price,
        String unit,
        String status,
        String imageUrl,
        UUID categoryId,
        String categoryName,
        Instant createdAt,
        Instant updatedAt
) {}