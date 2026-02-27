package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.service.ProductService;
import franchiseproject.product_service.specification.ProductSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + id));
    }

    @Override
    public List<Product> search(String name,
                                String productType,
                                String status,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                UUID categoryId) {

        Specification<Product> spec =
                ProductSpecification.filter(
                        name,
                        productType,
                        status,
                        minPrice,
                        maxPrice,
                        categoryId
                );

        return productRepository.findAll(spec);
    }

    @Override
    public Product uploadImage(UUID id, MultipartFile file) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        try {

            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destination = new File(uploadDir + fileName);

            file.transferTo(destination);

            product.setImageUrl("/uploads/" + fileName);

            return productRepository.save(product);

        } catch (IOException e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @Override
    public Product updateImage(UUID id, MultipartFile file) {

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (product.getImageUrl() != null) {
                String oldFileName = product.getImageUrl().replace("/uploads/", "");
                File oldFile = new File(uploadDir + oldFileName);

                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir + fileName);

            file.transferTo(dest);

            product.setImageUrl("/uploads/" + fileName);

            return productRepository.save(product);

        } catch (IOException e) {
            throw new RuntimeException("Update failed: " + e.getMessage());
        }
    }
}