package franchiseproject.inventory_service.repository;

import franchiseproject.inventory_service.model.FranchiseIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FranchiseIngredientRepository extends JpaRepository<FranchiseIngredient, UUID> {
}
