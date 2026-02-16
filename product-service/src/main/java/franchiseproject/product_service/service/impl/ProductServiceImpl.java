package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    @Override
    public List<Product> getAll(){
        return productRepository.findAll();
    }

}
