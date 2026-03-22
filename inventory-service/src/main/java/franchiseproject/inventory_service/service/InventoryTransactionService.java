package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.InventoryTransactionResponse;

import java.time.Instant;

public interface InventoryTransactionService {
    PageResponse<InventoryTransactionResponse> getTransactions(Instant from, Instant to, int page, int size);
}
