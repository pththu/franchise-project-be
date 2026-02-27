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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/product")
public class ProductController {

    ProductService productService;

    // (Optional) route cũ để tương thích: GET /product/getall
    @GetMapping("/getall")
    public List<Product> findAll() {
        return productService.getAll();
    }

    // ✅ View product list (paging + filter + search + sort): GET /product
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

    // ✅ View product details: GET /product/{id}
    @GetMapping("/{id}")
    public ProductDetailDTO getDetail(@PathVariable UUID id) {
        return productService.getDetail(id);
    }

    // ✅ CREATE: POST /product?categoryId=...
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailDTO create(@RequestBody Product product,
                                   @RequestParam UUID categoryId) {
        Product saved = productService.create(product, categoryId);
        return productService.getDetail(saved.getId());
    }

    // ✅ UPDATE: PUT /product/{id} (categoryId optional)
    @PutMapping("/{id}")
    public ProductDetailDTO update(@PathVariable UUID id,
                                   @RequestBody Product product,
                                   @RequestParam(required = false) UUID categoryId) {
        productService.update(id, product, categoryId);
        return productService.getDetail(id);
    }

    // ✅ DELETE: DELETE /product/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}