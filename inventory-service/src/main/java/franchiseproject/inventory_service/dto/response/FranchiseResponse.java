package franchiseproject.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseResponse {
    UUID id;
    String name;
    String address;
    Instant openedAt;
    Instant closedAt;
    Boolean isActive;
    Instant createdAt;
    Instant updatedAt;
}