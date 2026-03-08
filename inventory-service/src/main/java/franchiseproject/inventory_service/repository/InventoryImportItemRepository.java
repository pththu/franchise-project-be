package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.InventoryImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryImportItemRepository extends JpaRepository<InventoryImportItem, UUID> {
}