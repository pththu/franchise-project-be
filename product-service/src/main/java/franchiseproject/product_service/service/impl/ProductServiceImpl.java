package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;

    @Override
    public List<Product> getAll(){
        return productRepository.findAll();
    }

    @Override
    public Product uploadImage(UUID id, MultipartFile file) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {
            // Đường dẫn tuyệt đối
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            // Tạo folder nếu chưa có
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Tên file
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            File destination = new File(uploadDir + fileName);

            file.transferTo(destination);

            product.setImageUrl("/uploads/" + fileName);

            return productRepository.save(product);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}