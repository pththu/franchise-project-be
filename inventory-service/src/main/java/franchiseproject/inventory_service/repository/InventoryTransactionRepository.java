package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    Page<InventoryTransaction> findAllByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT tx FROM InventoryTransaction tx WHERE tx.productStock.locationId = :locationId")
    Page<InventoryTransaction> findByLocationId(@org.springframework.data.repository.query.Param("locationId") java.util.UUID locationId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT tx FROM InventoryTransaction tx WHERE tx.productStock.locationId = :locationId AND tx.createdAt >= :from AND tx.createdAt <= :to")
    Page<InventoryTransaction> findByLocationIdAndDateBetween(@org.springframework.data.repository.query.Param("locationId") java.util.UUID locationId, @org.springframework.data.repository.query.Param("from") java.time.Instant from, @org.springframework.data.repository.query.Param("to") java.time.Instant to, Pageable pageable);
}
