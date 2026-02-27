package franchiseproject.product_service.repository.spec;

import franchiseproject.product_service.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSpecifications {

    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> priceGte(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> priceLte(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}