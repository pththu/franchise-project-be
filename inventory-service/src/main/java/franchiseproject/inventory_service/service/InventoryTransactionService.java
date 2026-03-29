package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.InventoryTransactionResponse;

import java.time.Instant;

public interface InventoryTransactionService {
    PageResponse<InventoryTransactionResponse> getTransactions(java.util.UUID locationId, java.time.Instant from, java.time.Instant to, int page, int size);
}
