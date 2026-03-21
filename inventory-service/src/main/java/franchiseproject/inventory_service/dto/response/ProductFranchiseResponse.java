package franchiseproject.inventory_service.dto.response;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFranchiseResponse {
    UUID id;
    UUID productVariantId;
    UUID franchiseId;
    Integer quantity;
    Integer minStock;
    Instant createdAt;
    Instant updatedAt;
}
