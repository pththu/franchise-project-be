package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.InventoryImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryImportRepository extends JpaRepository<InventoryImport, UUID> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
}