package franchiseproject.product_service.controller;

import franchiseproject.product_service.dto.ApiResponse;
import franchiseproject.product_service.dto.request.CreateProductRequest;
import franchiseproject.product_service.dto.request.FilterProductsByCustomerRequest;
import franchiseproject.product_service.dto.request.SearchProductRequest;
import franchiseproject.product_service.dto.request.UpdateProductRequest;
import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.dto.response.ProductVariantDetailResponse;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.entity.ProductVariant;
import franchiseproject.product_service.enums.ProductStatus;
import franchiseproject.product_service.enums.ProductVariantStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
import franchiseproject.product_service.mapper.ProductMapper;
import franchiseproject.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/products")
public class ProductController {

    ProductService productService;
    ProductMapper productMapper;

    @GetMapping("/get-all")
    public ApiResponse<Page<ProductResponse>> getAll(@RequestParam("page") int page) {
        return ApiResponse.<Page<ProductResponse>>builder()
                .statusCode(200)
                .message("Get all products success")
                .data(productService.getAll(page)
                        .map(productMapper::toProductResponse))
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<Page<ProductResponse>> filterProductsByCustomer(@Valid @ModelAttribute FilterProductsByCustomerRequest request) {

        log.info("filterByCategoryAndPrice products API called with request: {} {}", request.getFromPrice(), request.getToPrice());

        if (request.getFromPrice() != null && request.getToPrice() != null) {
            if (request.getFromPrice().compareTo(request.getToPrice()) > 0) {
                throw new AppException(ErrorCode.INVALID_PRICE_RANGE);
            }
        }
        return ApiResponse.<Page<ProductResponse>>builder()
                .statusCode(200)
                .message("Filter product by category and price")
                .data(productService.filterProductsByCustomer(request)
                        .map(productMapper::toProductResponse))
                .build();
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<ProductResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.<ProductResponse>builder()
                .statusCode(200)
                .message("Get product by id")
                .data(productMapper.toProductResponse(productService.getById(id)))
                .build();
    }

    @PostMapping("/search-by-ids")
    public ApiResponse<java.util.List<ProductResponse>> getProductsByIds(@RequestBody java.util.List<java.util.UUID> ids) {
        return ApiResponse.<java.util.List<ProductResponse>>builder()
                .statusCode(200)
                .message("Get products by ids")
                .data(productService.getProductsByIds(ids))
                .build();
    }

    @GetMapping("/variant/{id}")
    public ApiResponse<ProductVariantDetailResponse> getProductVariant(@PathVariable UUID id) {
        ProductVariant variant = productService.getProductVariantById(id);
        Product parentProduct = variant.getProduct();

        ProductVariantDetailResponse response = ProductVariantDetailResponse.builder()
                .id(variant.getId())
                .size(variant.getSize())
                .color(variant.getColor())
                .price(variant.getPrice())
                .imageUrl(variant.getImageUrl())
                .productId(parentProduct.getId())
                .productName(parentProduct.getName())
                .brand(parentProduct.getBrand())
                .productType(parentProduct.getProductType())
                .build();

        return ApiResponse.<ProductVariantDetailResponse>builder()
                .statusCode(200)
                .message("Get product variant detail by id")
                .data(response)
                .build();
    }

    @PostMapping("/variants/bulk")
    public ApiResponse<java.util.List<ProductVariantDetailResponse>> getProductVariants(@RequestBody java.util.List<java.util.UUID> ids) {
        java.util.List<ProductVariant> variants = productService.getProductVariantsByIds(ids);
        java.util.List<ProductVariantDetailResponse> responses = variants.stream().map(v -> {
            var parentProduct = v.getProduct();
            return ProductVariantDetailResponse.builder()
                    .id(v.getId())
                    .size(v.getSize())
                    .color(v.getColor())
                    .price(v.getPrice())
                    .imageUrl(v.getImageUrl())
                    .productId(parentProduct.getId())
                    .productName(parentProduct.getName())
                    .brand(parentProduct.getBrand())
                    .productType(parentProduct.getProductType())
                    .build();
        }).collect(java.util.stream.Collectors.toList());

        return ApiResponse.<java.util.List<ProductVariantDetailResponse>>builder()
                .statusCode(200)
                .message("Get variants bulk success")
                .data(responses)
                .build();
    }


    @GetMapping("/search-dashboard")
    public ApiResponse<Page<ProductResponse>> search(@Valid @ModelAttribute SearchProductRequest request) {
        log.info("Search products API called with request: {} {}", request.getFromPrice(), request.getToPrice());

        if (request.getFromPrice() != null && request.getToPrice() != null) {
            if (request.getFromPrice().compareTo(request.getToPrice()) > 0) {
                throw new AppException(ErrorCode.INVALID_PRICE_RANGE);
            }
        }

        return ApiResponse.<Page<ProductResponse>>builder()
                .statusCode(200)
                .message("Search product with param")
                .data(productService.search(request).map(productMapper::toProductResponse))
                .build();
    }

    @GetMapping("/franchise/{locationId}")
    public ApiResponse<Page<ProductResponse>> getByFranchise(
            @PathVariable UUID locationId,
            @Valid @ModelAttribute SearchProductRequest request) {
        
        return ApiResponse.<Page<ProductResponse>>builder()
                .statusCode(200)
                .message("Get products by franchise success")
                .data(productService.searchByFranchise(locationId, request).map(productMapper::toProductResponse))
                .build();
    }

    @DeleteMapping("/inactive/{productId}")
    public ApiResponse<Boolean> deleteProduct(@PathVariable("productId") UUID productId) {
        Product product = productService.getById(productId);
        return ApiResponse.<Boolean>builder()
                .statusCode(200)
                .message("Deleted product")
                .data(productService.delete(product))
                .build();
    }

    @DeleteMapping("/{productId}/inactive-variant/{variantId}")
    public ApiResponse<Boolean> deleteProductVariant(
            @PathVariable("productId") UUID productId,
            @PathVariable("variantId") UUID variantId
    ) {
        Product product = productService.getById(productId);

        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new AppException(ErrorCode.PRODUCT_IS_DELETED);
        }

        ProductVariant variant = product.getVariants()
                .stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VARTIANT_NOT_FOUND));

        if (variant.getStatus() == ProductVariantStatus.INACTIVE) {
            throw new AppException(ErrorCode.VARTIAN_IS_DELETED);
        }

        return ApiResponse.<Boolean>builder()
                .statusCode(200)
                .message("Delete variant")
                .data(productService.deleteVariant(variant))
                .build();
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @RequestBody @Valid CreateProductRequest request) {

        return ApiResponse.<ProductResponse>builder()
                .statusCode(200)
                .message("Create product success")
                .data(productService.createProduct(request)) // ✅ FIX Ở ĐÂY
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable("id") UUID id,
            @RequestBody @Valid UpdateProductRequest request
    ) {

        // optional validate nhẹ (giữ consistent style với project)
        if (request == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        return ApiResponse.<ProductResponse>builder()
                .statusCode(200)
                .message("Update product success")
                .data(productService.updateProduct(id, request))
                .build();
    }

    // ✅ Backward-compatible: GET /api/products/getall
//    @GetMapping("/getall")
//    public List<ProductListItemResponse> findAll() {
//        return productService.getAllAsListItem();
//    }

    // ✅ List (paging + filter + search + sort): GET /api/products
    // Hỗ trợ cả q và name để tránh teammate lỗi khi đang dùng name
//    @GetMapping
//    public PageResponse<ProductListItemResponse> list(
//            @RequestParam(required = false) String q,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) UUID categoryId,
//            @RequestParam(required = false) BigDecimal minPrice,
//            @RequestParam(required = false) BigDecimal maxPrice,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt,desc") String sort
//    ) {
//        String keyword = (q != null && !q.isBlank()) ? q : name;
//        return productService.list(keyword, status, categoryId, minPrice, maxPrice, page, size, sort);
//    }

    // ✅ Detail: GET /api/products/{id}
    // CHỈ GIỮ 1 mapping /{id} để tránh Ambiguous mapping
//    @GetMapping("/{id}")
//    public ProductDetailResponse getDetail(@PathVariable UUID id) {
//        return productService.getDetail(id);
//    }

    // ✅ CREATE: POST /api/products?categoryId=...
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProductDetailResponse create(
//            @RequestBody Product product,
//            @RequestParam UUID categoryId
//    ) {
//        Product saved = productService.create(product, categoryId);
//        return productService.getDetail(saved.getId());
//    }

    // ✅ UPDATE: PUT /api/products/{id} (categoryId optional)
//    @PutMapping("/{id}")
//    public ProductDetailResponse update(
//            @PathVariable UUID id,
//            @RequestBody Product product,
//            @RequestParam(required = false) UUID categoryId
//    ) {
//        productService.update(id, product, categoryId);
//        return productService.getDetail(id);
//    }

    // ✅ DELETE: DELETE /api/products/{id}
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable UUID id) {
//        productService.delete(id);
//    }
//
//    // ✅ OPTIONAL: Search endpoint riêng (nếu team bạn vẫn cần)
//    // GET /api/products/search?name=...&productType=...&status=...
//    @GetMapping("/search")
//    public List<Product> searchProducts(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String productType,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) BigDecimal minPrice,
//            @RequestParam(required = false) BigDecimal maxPrice,
//            @RequestParam(required = false) UUID categoryId
//    ) {
//        return productService.search(
//                name,
//                productType,
//                status,
//                minPrice,
//                maxPrice,
//                categoryId
//        );
//    }

//    @PostMapping("/{id}/image")
//    public ResponseEntity<?> uploadImage(
//            @PathVariable UUID id,
//            @RequestParam("file") MultipartFile file
//    ) {
//        try {
//            Product product = productService.uploadImage(id, file);
//
//            return ResponseEntity.ok().body(
//                    Map.of(
//                            "status", 200,
//                            "message", "Upload thành công",
//                            "data", product.getImageUrl()
//                    )
//            );
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(
//                    Map.of(
//                            "status", 500,
//                            "message", e.getMessage()
//                    )
//            );
//        }
//    }

//    @PutMapping("/{id}/image")
//    public ResponseEntity<?> updateImage(
//            @PathVariable UUID id,
//            @RequestParam("file") MultipartFile file
//    ) {
//        try {
//            Product product = productService.updateImage(id, file);
//
//            return ResponseEntity.ok().body(
//                    Map.of(
//                            "status", 200,
//                            "message", "Update image thành công",
//                            "data", product.getImageUrl()
//                    )
//            );
//
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(404).body(
//                    Map.of(
//                            "status", 404,
//                            "message", e.getMessage()
//                    )
//            );
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(
//                    Map.of(
//                            "status", 500,
//                            "message", "Lỗi server"
//                    )
//            );
//        }
//    }
}