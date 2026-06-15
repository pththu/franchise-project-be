package franchiseproject.product_service.specification;

import franchiseproject.product_service.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> filter(
            String name,
            String productType,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            UUID categoryId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Search by name
            if (name != null && !name.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }

            // Filter by product type
            if (productType != null && !productType.isBlank()) {
                predicates.add(
                        cb.equal(root.get("product_type"), productType)
                );
            }

            // Filter by status
            if (status != null && !status.isBlank()) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            // Filter by min price
            if (minPrice != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("price"), minPrice)
                );
            }

            // Filter by max price
            if (maxPrice != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("price"), maxPrice)
                );
            }

            // Filter by category (ManyToOne)
            if (categoryId != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), categoryId)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}