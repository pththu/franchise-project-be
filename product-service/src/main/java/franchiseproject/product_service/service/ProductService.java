package franchiseproject.product_service.service;

import franchiseproject.product_service.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<Product> getAll();

    Product getById(UUID id);

    List<Product> search(
            String name,
            String productType,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            UUID categoryId
    );

    Product uploadImage(UUID id, MultipartFile file);

    Product updateImage(UUID id, MultipartFile file);
}