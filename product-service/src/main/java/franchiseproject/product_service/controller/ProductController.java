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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/product")
public class ProductController {

    ProductService productService;

    // ✅ NEW: View product list (paging + filter + search + sort)
    @GetMapping
    public PageResponse<ProductListItemDTO> list(
            @RequestParam(required = false) String q,
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

    // view list (route cũ)
    @GetMapping("/getall")
    public List<Product> findAll() {
        return productService.getAll();
    }

    // view details
    @GetMapping("/{id}")
    public Product getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    // create
    @PostMapping
    public ResponseEntity<Product> create(@RequestParam UUID categoryId,
                                          @RequestBody Product product) {
        Product created = productService.create(product, categoryId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // update
    @PutMapping("/{id}")
    public Product update(@PathVariable UUID id,
                          @RequestParam(required = false) UUID categoryId,
                          @RequestBody Product product) {
        return productService.update(id, product, categoryId);
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/detail")
    public ProductDetailDTO getDetail(@PathVariable UUID id) {
        return productService.getDetail(id);
    }
}