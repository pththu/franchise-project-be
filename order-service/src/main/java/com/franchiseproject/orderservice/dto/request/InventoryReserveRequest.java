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
public class InventoryReserveRequest {
    UUID locationId;
    List<InventoryItemRequest> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InventoryItemRequest {
        UUID productVariantId;
        Integer quantity;
    }
}
