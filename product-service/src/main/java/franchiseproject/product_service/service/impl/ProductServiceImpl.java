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

}