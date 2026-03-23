package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.request.CreateProductRequest;
import franchiseproject.product_service.dto.request.SearchProductRequest;
import franchiseproject.product_service.dto.request.UpdateProductRequest;
import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.entity.ProductVariant;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    Page<Product> getAll(int page);
    Product getById(UUID id);
    List<ProductResponse> getProductsByIds(List<UUID> ids);
    ProductVariant getProductVariantById (UUID id);
    List<ProductVariant> getProductVariantsByIds(List<UUID> ids);

    Page<Product> search(SearchProductRequest request);
    Page<Product> searchByFranchise(UUID locationId, SearchProductRequest request);
    boolean delete(Product product);
    boolean deleteVariant(ProductVariant variant);

//    Product create(Product product, UUID categoryId);
ProductResponse createProduct(CreateProductRequest request);

//    Product update(UUID id, Product product, UUID categoryId);
ProductResponse updateProduct(UUID id, UpdateProductRequest request);
//    Product uploadImage(UUID id, MultipartFile file);
//    Product updateImage(UUID id, MultipartFile file);
}