package franchiseproject.product_service.controller;

import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/products")
public class ProductController {

    ProductService productService;

    // Get all products
    @GetMapping
    public List<Product> findAll() {
        return productService.getAll();
    }

    // Get product by id
    @GetMapping("/{id}")
    public Product getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    // Search products
    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) UUID categoryId
    ) {
        return productService.search(
                name,
                productType,
                status,
                minPrice,
                maxPrice,
                categoryId
        );
    }

    // Upload image
    @PostMapping("/{id}/image")
    public Product uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.uploadImage(id, file);
    }

    // Update image
    @PutMapping("/{id}/image")
    public Product updateImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.updateImage(id, file);
    }
}