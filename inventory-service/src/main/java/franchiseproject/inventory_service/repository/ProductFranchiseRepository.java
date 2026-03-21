package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.entity.ProductFranchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductFranchiseRepository extends JpaRepository<ProductFranchise, UUID> {

    List<ProductFranchise> findByFranchiseId(UUID id);
}
