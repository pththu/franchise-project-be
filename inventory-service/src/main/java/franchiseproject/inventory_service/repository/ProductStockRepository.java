package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {
    
    Page<ProductStock> findByLocationId(UUID locationId, Pageable pageable);
    
    @Query("SELECT ps FROM ProductStock ps WHERE ps.quantity <= ps.minStock")
    Page<ProductStock> findLowStock(Pageable pageable);
    
    @Query("SELECT ps FROM ProductStock ps WHERE ps.locationId = :locationId AND ps.quantity <= ps.minStock")
    Page<ProductStock> findLowStockByLocation(@Param("locationId") UUID locationId, Pageable pageable);

    Optional<ProductStock> findByProductVariantIdAndLocationId(UUID productVariantId, UUID locationId);

    @Query("SELECT ps.productVariantId FROM ProductStock ps WHERE ps.locationId = :locationId AND ps.quantity > 0")
    List<UUID> findInStockVariantIds(@Param("locationId") UUID locationId);
}
