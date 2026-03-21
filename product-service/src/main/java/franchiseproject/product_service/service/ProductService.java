package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.request.SearchProductRequest;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.entity.ProductVariant;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ProductService {

    Page<Product> getAll(int page);
    Product getById(UUID id);
    ProductVariant getProductVariantById (UUID id);
    Page<Product> search(SearchProductRequest request);
    boolean delete(Product product);
    boolean deleteVariant(ProductVariant variant);

//    Product create(Product product, UUID categoryId);
//    Product update(UUID id, Product product, UUID categoryId);
//    Product uploadImage(UUID id, MultipartFile file);
//    Product updateImage(UUID id, MultipartFile file);
}