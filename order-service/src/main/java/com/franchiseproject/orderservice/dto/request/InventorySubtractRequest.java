package com.franchiseproject.orderservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventorySubtractRequest {
    UUID locationId;
    List<InventoryReserveRequest.InventoryItemRequest> items; // Tái sử dụng static class trong ReserveRequest cho gọn
    boolean fromReserved;
    String referenceId;
    String referenceType;
    String createdBy;
}
