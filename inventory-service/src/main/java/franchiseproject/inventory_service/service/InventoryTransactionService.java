package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.entity.InventoryTransaction;

import java.util.List;

public interface InventoryTransactionService {
    List<InventoryTransaction> getAll();
}
