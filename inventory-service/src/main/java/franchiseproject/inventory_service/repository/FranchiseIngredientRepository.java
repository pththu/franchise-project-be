package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.FranchiseIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FranchiseIngredientRepository extends JpaRepository<FranchiseIngredient, UUID> {
    // 1 View inventory theo franchise
    List<FranchiseIngredient> findByFranchiseId(UUID franchiseId);

    // 5 search theo product trong 1 franchise
    List<FranchiseIngredient> findByProductNameContainingIgnoreCaseAndFranchise_Id(String productName, UUID franchiseId);

    // 6 filter theo product tất cả franchise
    List<FranchiseIngredient> findByProductNameContainingIgnoreCase(String productName);
}
