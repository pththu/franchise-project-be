package franchiseproject.product_service.service;

import franchiseproject.product_service.model.Product;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.List;

public interface ProductService {
    List<Product> getAll();
    Product uploadImage(UUID id, MultipartFile file);
    Product updateImage(UUID id, MultipartFile file);
}

