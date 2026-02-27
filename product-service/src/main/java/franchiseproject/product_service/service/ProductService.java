package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.PageResponse;
import franchiseproject.product_service.dto.ProductDetailDTO;
import franchiseproject.product_service.dto.ProductListItemDTO;
import franchiseproject.product_service.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDetailDTO getDetail(UUID id);
    List<Product> getAll();
    Product getById(UUID id);
    Product create(Product product, UUID categoryId);
    Product update(UUID id, Product product, UUID categoryId);

    void delete(UUID id);

    // ✅ NEW: view product list
    PageResponse<ProductListItemDTO> list(
            String q,
            String status,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    );
}