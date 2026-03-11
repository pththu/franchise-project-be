package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.FranchiseIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FranchiseIngredientRepository extends JpaRepository<FranchiseIngredient, UUID> {
    List<FranchiseIngredient> findByFranchiseId(UUID franchiseId);


    List<FranchiseIngredient> findByProductNameContainingIgnoreCaseAndFranchise_Id(String productName, UUID franchiseId);


    List<FranchiseIngredient> findByProductNameContainingIgnoreCase(String productName);

    Optional<FranchiseIngredient> findByFranchise_IdAndProductId(UUID franchiseId, UUID productId);
}
