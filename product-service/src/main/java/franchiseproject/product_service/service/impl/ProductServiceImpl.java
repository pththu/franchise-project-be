package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.PageResponse;
import franchiseproject.product_service.dto.ProductDetailDTO;
import franchiseproject.product_service.dto.ProductListItemDTO;
import franchiseproject.product_service.exception.NotFoundException;
import franchiseproject.product_service.model.Category;
import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.repository.spec.ProductSpecifications;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;

    // ✅ (Optional) dùng nội bộ hoặc route cũ trả entity
    @Override
    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // ✅ Dùng cho /api/products/getall (trả DTO để tránh 500 do serialize Entity)
    @Override
    @Transactional(readOnly = true)
    public List<ProductListItemDTO> getAllAsListItem() {
        // category là LAZY, nhưng đang ở trong @Transactional => đọc được categoryId/name
        return productRepository.findAll().stream()
                .map(p -> new ProductListItemDTO(
                        p.getId(),
                        p.getProductType(),
                        p.getName(),
                        p.getPrice(),
                        p.getUnit(),
                        p.getStatus(),
                        p.getImageUrl(),
                        p.getCategory() != null ? p.getCategory().getId() : null,
                        p.getCategory() != null ? p.getCategory().getName() : null,
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional
    public Product create(Product product, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));

        // đảm bảo tạo mới
        product.setId(null);
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(UUID id, Product product, UUID categoryId) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
            existing.setCategory(category);
        }

        if (product.getProductType() != null) existing.setProductType(product.getProductType());
        if (product.getName() != null) existing.setName(product.getName());
        if (product.getDescription() != null) existing.setDescription(product.getDescription());
        if (product.getPrice() != null) existing.setPrice(product.getPrice());
        if (product.getUnit() != null) existing.setUnit(product.getUnit());
        if (product.getStatus() != null) existing.setStatus(product.getStatus());
        if (product.getImageUrl() != null) existing.setImageUrl(product.getImageUrl());

        return productRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    // ✅ View product details (DTO)
    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getDetail(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        return new ProductDetailDTO(
                p.getId(),
                p.getProductType(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getUnit(),
                p.getStatus(),
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    // ✅ View product list (paging + filter + search + sort)
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListItemDTO> list(
            String q,
            String status,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    ) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Product> spec = Specification.allOf(
                ProductSpecifications.nameContains(q),
                ProductSpecifications.hasStatus(status),
                ProductSpecifications.hasCategory(categoryId),
                ProductSpecifications.priceGte(minPrice),
                ProductSpecifications.priceLte(maxPrice)
        );

        Page<Product> result = productRepository.findAll(spec, pageable);

        Page<ProductListItemDTO> dtoPage = result.map(p -> new ProductListItemDTO(
                p.getId(),
                p.getProductType(),
                p.getName(),
                p.getPrice(),
                p.getUnit(),
                p.getStatus(),
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        ));

        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    // sort format: "createdAt,desc" or "price,asc"
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction direction =
                (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }
}