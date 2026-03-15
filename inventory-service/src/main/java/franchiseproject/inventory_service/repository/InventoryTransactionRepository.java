package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    @Query("""
            SELECT it
            FROM InventoryTransaction it
            JOIN FETCH it.franchiseIngredient fi
            JOIN FETCH fi.franchise f
            ORDER BY it.createdAt DESC
            """)
    List<InventoryTransaction> findAllWithRelations();

    @Query("""
            SELECT it
            FROM InventoryTransaction it
            JOIN FETCH it.franchiseIngredient fi
            JOIN FETCH fi.franchise f
            WHERE f.id = :franchiseId
            ORDER BY it.createdAt DESC
            """)
    List<InventoryTransaction> findAllByFranchiseId(@Param("franchiseId") UUID franchiseId);

    @Query("""
            SELECT it
            FROM InventoryTransaction it
            JOIN FETCH it.franchiseIngredient fi
            JOIN FETCH fi.franchise f
            WHERE it.id = :id
            """)
    Optional<InventoryTransaction> findDetailById(@Param("id") UUID id);
}