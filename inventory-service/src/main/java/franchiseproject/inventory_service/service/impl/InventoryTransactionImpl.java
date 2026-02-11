package franchiseproject.inventory_service.service.impl;

import franchiseproject.inventory_service.model.InventoryTransaction;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import franchiseproject.inventory_service.service.FranchiseIngredientService;
import franchiseproject.inventory_service.service.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionImpl implements InventoryTransactionService {
    InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public List<InventoryTransaction> getAll() {
        return inventoryTransactionRepository.findAll();
    }
}
