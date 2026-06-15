package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    Page<StockTransfer> findAll(Pageable pageable);
    Page<StockTransfer> findByFromLocationId(UUID fromLocationId, Pageable pageable);
    java.util.List<StockTransfer> findByReferenceRequestId(UUID referenceRequestId);
}
