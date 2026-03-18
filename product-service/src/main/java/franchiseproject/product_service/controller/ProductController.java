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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/products")
public class ProductController {

    ProductService productService;

    // ✅ Backward-compatible: GET /api/products/getall
    @GetMapping("/getall")
    public List<ProductListItemDTO> findAll() {
        return productService.getAllAsListItem();
    }

    // ✅ List (paging + filter + search + sort): GET /api/products
    // Hỗ trợ cả q và name để tránh teammate lỗi khi đang dùng name
    @GetMapping
    public PageResponse<ProductListItemDTO> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String keyword = (q != null && !q.isBlank()) ? q : name;
        return productService.list(keyword, status, categoryId, minPrice, maxPrice, page, size, sort);
    }

    // ✅ Detail: GET /api/products/{id}
    // CHỈ GIỮ 1 mapping /{id} để tránh Ambiguous mapping
    @GetMapping("/{id}")
    public ProductDetailDTO getDetail(@PathVariable UUID id) {
        return productService.getDetail(id);
    }

    // ✅ CREATE: POST /api/products?categoryId=...
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailDTO create(
            @RequestBody Product product,
            @RequestParam UUID categoryId
    ) {
        Product saved = productService.create(product, categoryId);
        return productService.getDetail(saved.getId());
    }

    // ✅ UPDATE: PUT /api/products/{id} (categoryId optional)
    @PutMapping("/{id}")
    public ProductDetailDTO update(
            @PathVariable UUID id,
            @RequestBody Product product,
            @RequestParam(required = false) UUID categoryId
    ) {
        productService.update(id, product, categoryId);
        return productService.getDetail(id);
    }

    // ✅ DELETE: DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }

    // ✅ OPTIONAL: Search endpoint riêng (nếu team bạn vẫn cần)
    // GET /api/products/search?name=...&productType=...&status=...
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

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Product product = productService.uploadImage(id, file);

            return ResponseEntity.ok().body(
                    Map.of(
                            "status", 200,
                            "message", "Upload thành công",
                            "data", product.getImageUrl()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of(
                            "status", 500,
                            "message", e.getMessage()
                    )
            );
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Product product = productService.updateImage(id, file);

            return ResponseEntity.ok().body(
                    Map.of(
                            "status", 200,
                            "message", "Update image thành công",
                            "data", product.getImageUrl()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                    Map.of(
                            "status", 404,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of(
                            "status", 500,
                            "message", "Lỗi server"
                    )
            );
        }
    }
}