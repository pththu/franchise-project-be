package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.request.SearchProductRequest;
import franchiseproject.product_service.dto.response.*;
import franchiseproject.product_service.entity.ProductVariant;
import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductStatus;
import franchiseproject.product_service.enums.ProductVariantStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
import franchiseproject.product_service.exception.NotFoundException;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.mapper.ProductMapper;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.repository.ProductVariantRepository;
import franchiseproject.product_service.repository.spec.ProductSpecifications;
import franchiseproject.product_service.service.ProductService;
import franchiseproject.product_service.specification.ProductSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;


    @Override
    public Page<Product> getAll(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by("name").ascending()
        );

        return productRepository.findAll(pageable);
    }
    @Override
    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductVariant getProductVariantById(UUID id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VARTIANT_NOT_FOUND));
    }

    /**
     * xóa (inactive) product thì kéo theo xóa (inactive) variants của sản phẩm đó
     * @param product
     * @return
     */
    @Override
    public boolean delete(Product product) {
        if (product == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        product.setStatus(ProductStatus.INACTIVE);
        Product deleted = productRepository.save(product);
        log.info("deleted: {}", deleted.getName());

        if (deleted == null) {
            return false;
        }

        List<ProductVariant> variants = productVariantRepository.findAllByProductId(product.getId());
        if (variants.size() <= 0) {
            log.info("Variants empty");
        }

        variants.forEach(v -> {
            v.setStatus(ProductVariantStatus.INACTIVE);
            ProductVariant pv = productVariantRepository.save(v);
            if (pv == null) {
                new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        });
        return true;
    }

    @Override
    public boolean deleteVariant(ProductVariant variant) {

        if (variant == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        variant.setStatus(ProductVariantStatus.INACTIVE);
        ProductVariant pv = productVariantRepository.save(variant);

        return  pv == null ? false : true;
    }

    /**
     * tìm kiếm với nhiều parameter
     * @param request
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(SearchProductRequest request) {

        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty())
                ? request.getKeyword().trim() : null;

        String categoryName = (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty())
                ? request.getCategoryName().trim() : null;

        ProductStatus status = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
                ? ProductStatus.valueOf(request.getStatus().trim()) : null;

        ProductColor color = (request.getColor() != null && !request.getColor().trim().isEmpty())
                ? ProductColor.valueOf(request.getColor().trim()) : null;

        ProductSize size = (request.getSize() != null && !request.getSize().trim().isEmpty())
                ? ProductSize.valueOf(request.getSize().trim()) : null;

        Pageable pageable = PageRequest.of(
                request.getPage().intValue(),
                request.getSizePage().intValue(),
                Sort.by(request.getSortBy()).ascending()
        );

        return productRepository.search(
                keyword,
                categoryName,
                status,
                color,
                size,
                request.getFromPrice(),
                request.getToPrice(),
                pageable
        );
    }

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