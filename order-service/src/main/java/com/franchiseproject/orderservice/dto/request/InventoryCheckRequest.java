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
public class InventoryCheckRequest {
    UUID locationId;
    List<InventoryReserveRequest.InventoryItemRequest> items;
}
