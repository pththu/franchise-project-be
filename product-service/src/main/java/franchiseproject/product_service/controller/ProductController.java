package franchiseproject.product_service.controller;

import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("api/products")
public class ProductController {

    ProductService productService;

    @GetMapping
    public List<Product> findAll() {
        return productService.getAll();
    }

    @PostMapping("/{id}/image")
    public Product uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.uploadImage(id, file);
    }

    @PutMapping("/{id}/image")
    public Product updateImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.updateImage(id, file);
    }
}