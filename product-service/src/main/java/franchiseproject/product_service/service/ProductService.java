package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.response.PageResponse;
import franchiseproject.product_service.dto.response.ProductDetailResponse;
import franchiseproject.product_service.dto.response.ProductListItemResponse;
import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

//    List<Product> getAll();

    Page<ProductResponse> getAll(int page);

//    List<ProductListItemResponse> getAllAsListItem();
//
//    Product getById(UUID id);
//
//    ProductDetailResponse getDetail(UUID id);
//
//    Product create(Product product, UUID categoryId);
//
//    Product update(UUID id, Product product, UUID categoryId);
//
//    void delete(UUID id);
//
//    PageResponse<ProductListItemResponse> list(
//            String q,
//            String status,
//            UUID categoryId,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            int page,
//            int size,
//            String sort
//    );
//
//    List<Product> search(
//            String name,
//            String productType,
//            String status,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            UUID categoryId
//    );
//
//    Product uploadImage(UUID id, MultipartFile file);
//
//    Product updateImage(UUID id, MultipartFile file);
}