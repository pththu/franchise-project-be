package franchiseproject.product_service.repository;

import franchiseproject.product_service.model.Category;
import franchiseproject.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
