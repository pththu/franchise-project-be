package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.model.InventoryTransaction;

import java.util.List;

public interface InventoryTransactionService {
    List<InventoryTransaction> getAll();
}
