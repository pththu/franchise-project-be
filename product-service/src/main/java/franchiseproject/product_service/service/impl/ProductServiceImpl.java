package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.response.PageResponse;
import franchiseproject.product_service.dto.response.ProductDetailResponse;
import franchiseproject.product_service.dto.response.ProductListItemResponse;
import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.exception.NotFoundException;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.mapper.ProductMapper;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.repository.spec.ProductSpecifications;
import franchiseproject.product_service.service.ProductService;
import franchiseproject.product_service.specification.ProductSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;

//    @Override
//    @Transactional(readOnly = true)
//    public List<Product> getAll() {
//        return productRepository.findAll();
//    }

    @Override
    public Page<ProductResponse> getAll(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by("name").ascending()
        );

        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductListItemResponse> getAllAsListItem() {
//        return productRepository.findAll().stream()
//                .map(p -> new ProductListItemResponse(
//                        p.getId(),
//                        p.getProductType(),
//                        p.getName(),
//                        p.getPrice(),
//                        p.getUnit(),
//                        p.getStatus(),
//                        p.getImageUrl(),
//                        p.getCategory() != null ? p.getCategory().getId() : null,
//                        p.getCategory() != null ? p.getCategory().getName() : null,
//                        p.getCreatedAt(),
//                        p.getUpdatedAt()
//                ))
//                .toList();
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public Product getById(UUID id) {
//        return productRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
//    }
//    @Override
//    public List<Product> search(String name,
//                                String productType,
//                                String status,
//                                BigDecimal minPrice,
//                                BigDecimal maxPrice,
//                                UUID categoryId) {
//
//        Specification<Product> spec =
//                ProductSpecification.filter(
//                        name,
//                        productType,
//                        status,
//                        minPrice,
//                        maxPrice,
//                        categoryId
//                );
//
//        return productRepository.findAll(spec);
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public ProductDetailResponse getDetail(UUID id) {
//        Product p = getById(id);
//
//        return new ProductDetailResponse(
//                p.getId(),
//                p.getProductType(),
//                p.getName(),
//                p.getDescription(),
//                p.getPrice(),
//                p.getUnit(),
//                p.getStatus(),
//                p.getImageUrl(),
//                p.getCategory() != null ? p.getCategory().getId() : null,
//                p.getCategory() != null ? p.getCategory().getName() : null,
//                p.getCreatedAt(),
//                p.getUpdatedAt()
//        );
//    }

//    @Override
//    @Transactional
//    public Product create(Product product, UUID categoryId) {
//        Category category = categoryRepository.findById(categoryId)
//                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
//
//        product.setId(null);
//        product.setCategory(category);
//
//        return productRepository.save(product);
//    }

//    @Override
//    @Transactional
//    public Product update(UUID id, Product product, UUID categoryId) {
//        Product existing = getById(id);
//
//        if (categoryId != null) {
//            Category category = categoryRepository.findById(categoryId)
//                    .orElseThrow(() -> new NotFoundException("Category not found"));
//            existing.setCategory(category);
//        }
//
//        if (product.getProductType() != null) existing.setProductType(product.getProductType());
//        if (product.getName() != null) existing.setName(product.getName());
//        if (product.getDescription() != null) existing.setDescription(product.getDescription());
//        if (product.getPrice() != null) existing.setPrice(product.getPrice());
//        if (product.getUnit() != null) existing.setUnit(product.getUnit());
//        if (product.getStatus() != null) existing.setStatus(product.getStatus());
//        if (product.getImageUrl() != null) existing.setImageUrl(product.getImageUrl());
//
//        return productRepository.save(existing);
//    }

//    @Override
//    @Transactional
//    public void delete(UUID id) {
//        if (!productRepository.existsById(id)) {
//            throw new NotFoundException("Product not found");
//        }
//        productRepository.deleteById(id);
//    }

//    @Override
//    @Transactional(readOnly = true)
//    public PageResponse<ProductListItemResponse> list(
//            String q,
//            String status,
//            UUID categoryId,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            int page,
//            int size,
//            String sort
//    ) {
//        Sort sortObj = parseSort(sort);
//        Pageable pageable = PageRequest.of(page, size, sortObj);
//
//        Specification<Product> spec = Specification.allOf(
//                ProductSpecifications.nameContains(q),
//                ProductSpecifications.hasStatus(status),
//                ProductSpecifications.hasCategory(categoryId),
//                ProductSpecifications.priceGte(minPrice),
//                ProductSpecifications.priceLte(maxPrice)
//        );
//
//        Page<Product> result = productRepository.findAll(spec, pageable);
//
//        Page<ProductListItemResponse> dtoPage = result.map(p -> new ProductListItemResponse(
//                p.getId(),
//                p.getProductType(),
//                p.getName(),
//                p.getPrice(),
//                p.getUnit(),
//                p.getStatus(),
//                p.getImageUrl(),
//                p.getCategory() != null ? p.getCategory().getId() : null,
//                p.getCategory() != null ? p.getCategory().getName() : null,
//                p.getCreatedAt(),
//                p.getUpdatedAt()
//        ));

//        return new PageResponse<>(
//                dtoPage.getContent(),
//                dtoPage.getNumber(),
//                dtoPage.getSize(),
//                dtoPage.getTotalElements(),
//                dtoPage.getTotalPages()
//        );
//    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0];

        Sort.Direction direction =
                (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

//    @Override
//    public Product uploadImage(UUID id, MultipartFile file) {
//        Product product = getById(id);
//
//        try {
//            String uploadDir = System.getProperty("user.dir") + "/uploads/";
//            File dir = new File(uploadDir);
//
//            if (!dir.exists()) dir.mkdirs();
//
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            File dest = new File(uploadDir + fileName);
//
//            file.transferTo(dest);
//
//            product.setImageUrl("/uploads/" + fileName);
//
//            return productRepository.save(product);
//
//        } catch (IOException e) {
//            throw new RuntimeException("Upload failed");
//        }
//    }

//    @Override
//    public Product updateImage(UUID id, MultipartFile file) {
//
//        Product product = getById(id);
//
//        try {
//            String uploadDir = System.getProperty("user.dir") + "/uploads/";
//            File dir = new File(uploadDir);
//
//            if (!dir.exists()) dir.mkdirs();
//
//            if (product.getImageUrl() != null) {
//                String oldFileName = product.getImageUrl().replace("/uploads/", "");
//                File oldFile = new File(uploadDir + oldFileName);
//                if (oldFile.exists()) oldFile.delete();
//            }
//
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            File dest = new File(uploadDir + fileName);
//
//            file.transferTo(dest);
//
//            product.setImageUrl("/uploads/" + fileName);
//
//            return productRepository.save(product);
//
//        } catch (IOException e) {
//            throw new RuntimeException("Update failed");
//        }
//    }
}