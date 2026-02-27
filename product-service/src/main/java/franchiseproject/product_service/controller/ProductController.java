package franchiseproject.product_service.controller;

import franchiseproject.product_service.dto.PageResponse;
import franchiseproject.product_service.dto.ProductDetailDTO;
import franchiseproject.product_service.dto.ProductListItemDTO;
import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
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


    @GetMapping("/getall")
    public List<ProductListItemDTO> findAll() {
        return productService.getAllAsListItem();
    }


    @GetMapping
    public PageResponse<ProductListItemDTO> list(
            @RequestParam(required = false) String q,

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
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return productService.list(q, status, categoryId, minPrice, maxPrice, page, size, sort);
    }

    @GetMapping("/{id}")
    public ProductDetailDTO getDetail(@PathVariable UUID id) {
        return productService.getDetail(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailDTO create(
            @RequestBody Product product,
            @RequestParam UUID categoryId
    ) {
        Product saved = productService.create(product, categoryId);
        return productService.getDetail(saved.getId());
    }


    @PutMapping("/{id}")
    public ProductDetailDTO update(
            @PathVariable UUID id,
            @RequestBody Product product,
            @RequestParam(required = false) UUID categoryId
    ) {

        productService.update(id, product, categoryId);
        return productService.getDetail(id);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);

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