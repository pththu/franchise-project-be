package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.entity.StockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, UUID> {
    List<StockRequest> findByFranchiseId(UUID franchiseId);
}
