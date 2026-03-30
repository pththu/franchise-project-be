package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.client.InventoryClient;
import franchiseproject.product_service.client.TranslateClient; // ✅ ADD
import franchiseproject.product_service.dto.request.*;
import franchiseproject.product_service.dto.response.*;
import franchiseproject.product_service.entity.ProductVariant;
import franchiseproject.product_service.enums.ProductColor;
import franchiseproject.product_service.enums.ProductSize;
import franchiseproject.product_service.enums.ProductStatus;
import franchiseproject.product_service.enums.ProductVariantStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.mapper.ProductMapper;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.repository.ProductRepository;
import franchiseproject.product_service.repository.ProductVariantRepository;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;
    InventoryClient inventoryClient;

    private final TranslateClient translateClient; // ✅ ADD

    private String convertListToJson(List<String> urls) {
        try {
            return new ObjectMapper().writeValueAsString(urls);
        } catch (Exception e) {
            throw new RuntimeException("Convert imageUrls failed");
        }
    }

    @Override
    public Page<Product> getAll(int page) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by("name").ascending());
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> products = productRepository.findAllById(ids);
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariant getProductVariantById(UUID id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VARTIANT_NOT_FOUND));
    }

    @Override
    public List<ProductVariant> getProductVariantsByIds(List<UUID> ids) {
        return productVariantRepository.findAllById(ids);
    }

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

        return pv == null ? false : true;
    }

    @Override
    public Page<Product> filterProductsByCustomer(FilterProductsByCustomerRequest request) {

        if (request.getFranchiseId() != null) {
            List<UUID> variantIds = inventoryClient.getInStockVariantIds(request.getFranchiseId());
            if (variantIds == null || variantIds.isEmpty()) {
                return Page.empty();
            }
        }

        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty())
                ? request.getKeyword().trim()
                : null;

        List<ProductColor> colors = request.getColors() != null ? request.getColors()
                .stream()
                .map(c -> (c != null && !c.trim().isEmpty())
                        ? ProductColor.valueOf(c.trim())
                        : null)
                .toList() : null;

        List<ProductSize> sizes = request.getSizes() != null ? request.getSizes()
                .stream()
                .map(size -> (size != null && !size.trim().isEmpty())
                        ? ProductSize.valueOf(size.trim())
                        : null)
                .toList() : null;

        Pageable pageable = PageRequest.of(
                request.getPage().intValue(),
                request.getSizePage().intValue(),
                Sort.by(request.getSortBy()).ascending());

        return productRepository.filterProducts(
                request.getCategories(),
                request.getFromPrice(),
                request.getToPrice(),
                keyword,
                colors,
                sizes,
                request.getProductIds(),
                pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(SearchProductRequest request) {

        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty())
                ? request.getKeyword().trim()
                : null;

        String categoryName = (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty())
                ? request.getCategoryName().trim()
                : null;

        ProductStatus status = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
                ? ProductStatus.valueOf(request.getStatus().trim())
                : null;

        ProductColor color = (request.getColor() != null && !request.getColor().trim().isEmpty())
                ? ProductColor.valueOf(request.getColor().trim())
                : null;

        ProductSize size = (request.getSize() != null && !request.getSize().trim().isEmpty())
                ? ProductSize.valueOf(request.getSize().trim())
                : null;

        Pageable pageable = PageRequest.of(
                request.getPage().intValue(),
                request.getSizePage().intValue(),
                Sort.by(request.getSortBy()).ascending());

        return productRepository.search(
                keyword,
                categoryName,
                status,
                color,
                size,
                request.getFromPrice(),
                request.getToPrice(),
                pageable);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGOTY_NOT_FOUND));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .productType("DEFAULT")
                .unit("Cái")
                .brand(
                        request.getBrand() != null && !request.getBrand().isBlank()
                                ? request.getBrand()
                                : "No Brand")
                .build();

        Product savedProduct = productRepository.save(product);

        List<ProductVariant> variants = request.getVariants()
                .stream()
                .map(v -> {

            ProductColor color = ProductColor.valueOf(v.getColor().toUpperCase());
            ProductSize size = ProductSize.valueOf(v.getSize().toUpperCase());

            String imageUrl = (v.getImageUrls() != null && !v.getImageUrls().isEmpty())
                    ? convertListToJson(v.getImageUrls())
                    : null;

            return ProductVariant.builder()
                    .product(savedProduct)
                    .color(color)
                    .size(size)
                    .price(v.getPrice())
                    .salePrice(v.getPrice())
                    .quantity(v.getStock())
                    .imageUrl(imageUrl)
                    .status(ProductVariantStatus.ACTIVE)
                    .build();
        }).collect(Collectors.toList());

        productVariantRepository.saveAll(variants);
        savedProduct.setVariants(variants);

        applyProductTranslations(savedProduct);
        System.out.println("12345");
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        String previousName = product.getName();
        String previousDescription = product.getDescription();
        String previousBrand = product.getBrand();

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            product.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGOTY_NOT_FOUND));
            product.setCategory(category);
        }

        if (request.getProductType() != null && !request.getProductType().isBlank()) {
            product.setProductType(request.getProductType());
        }

        if (request.getUnit() != null && !request.getUnit().isBlank()) {
            product.setUnit(request.getUnit());
        }

        if (request.getBrand() != null && !request.getBrand().isBlank()) {
            product.setBrand(request.getBrand());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getVariants() != null) {
            mergeProductVariants(product, request.getVariants());
        }

        boolean needsTranslation = hasTextChanged(previousName, product.getName())
                || hasTextChanged(previousDescription, product.getDescription())
                || hasTextChanged(previousBrand, product.getBrand());

        log.info("product: {}: ", product.getNameEn());
        if (needsTranslation) {
            applyProductTranslations(product);
        } else {
            log.info("Skip translation because name/description/brand unchanged for product {}", product.getId());
        }

        Product updatedProduct = productRepository.save(product);
        updatedProduct.setVariants(productVariantRepository.findAllByProductId(updatedProduct.getId()));
        log.info("updatedProduct: {}: ", updatedProduct.getNameEn());
        return productMapper.toProductResponse(updatedProduct);
    }

    private void mergeProductVariants(Product product, List<UpdateProductVariantRequest> requestVariants) {
        List<ProductVariant> existingVariants = productVariantRepository.findAllByProductId(product.getId());
        Map<UUID, ProductVariant> existingById = existingVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

        Set<UUID> retainedVariantIds = new HashSet<>();
        List<ProductVariant> variantsToSave = new ArrayList<>();

        for (UpdateProductVariantRequest requestVariant : requestVariants) {
            if (requestVariant.getId() != null) {
                ProductVariant existingVariant = existingById.get(requestVariant.getId());
                if (existingVariant == null) {
                    throw new AppException(ErrorCode.VARTIANT_NOT_FOUND);
                }

                updateVariantFields(existingVariant, requestVariant);
                retainedVariantIds.add(existingVariant.getId());
                variantsToSave.add(existingVariant);
                continue;
            }

            ProductVariant newVariant = buildNewVariant(product, requestVariant);
            variantsToSave.add(newVariant);
        }

        for (ProductVariant existingVariant : existingVariants) {
            if (!retainedVariantIds.contains(existingVariant.getId())) {
                existingVariant.setStatus(ProductVariantStatus.INACTIVE);
                variantsToSave.add(existingVariant);
            }
        }

        if (!variantsToSave.isEmpty()) {
            productVariantRepository.saveAll(variantsToSave);
        }
    }

    private ProductVariant buildNewVariant(Product product, UpdateProductVariantRequest requestVariant) {
        ProductColor color = ProductColor.valueOf(requestVariant.getColor().toUpperCase());
        ProductSize size = ProductSize.valueOf(requestVariant.getSize().toUpperCase());

        String imageUrl = requestVariant.getImageUrls() != null
                ? convertListToJson(requestVariant.getImageUrls())
                : null;

        ProductVariantStatus status = (requestVariant.getStatus() != null && !requestVariant.getStatus().isBlank())
                ? ProductVariantStatus.valueOf(requestVariant.getStatus().toUpperCase())
                : ProductVariantStatus.ACTIVE;

        return ProductVariant.builder()
                .product(product)
                .color(color)
                .size(size)
                .price(requestVariant.getPrice())
                .salePrice(requestVariant.getPrice())
                .quantity(requestVariant.getStock() != null ? requestVariant.getStock() : 0)
                .imageUrl(imageUrl)
                .status(status)
                .build();
    }

    private void updateVariantFields(ProductVariant variant, UpdateProductVariantRequest requestVariant) {
        if (requestVariant.getColor() != null && !requestVariant.getColor().isBlank()) {
            variant.setColor(ProductColor.valueOf(requestVariant.getColor().toUpperCase()));
        }

        if (requestVariant.getSize() != null && !requestVariant.getSize().isBlank()) {
            variant.setSize(ProductSize.valueOf(requestVariant.getSize().toUpperCase()));
        }

        if (requestVariant.getPrice() != null) {
            variant.setPrice(requestVariant.getPrice());
            variant.setSalePrice(requestVariant.getPrice());
        }

        if (requestVariant.getImageUrls() != null) {
            variant.setImageUrl(convertListToJson(requestVariant.getImageUrls()));
        }

        if (requestVariant.getStatus() != null && !requestVariant.getStatus().isBlank()) {
            variant.setStatus(ProductVariantStatus.valueOf(requestVariant.getStatus().toUpperCase()));
        }
    }

    private boolean hasTextChanged(String oldValue, String newValue) {
        return !normalizeText(oldValue).equals(normalizeText(newValue));
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private void applyProductTranslations(Product product) {
        List<String> sourceTexts = new ArrayList<>();
        sourceTexts.add(product.getName());
        sourceTexts.add(product.getDescription());
        sourceTexts.add(product.getBrand());

        Map<String, List<String>> translatedByLanguage = translateClient.translateByLanguage(sourceTexts,
                List.of("en", "ja"));

        log.info("translatedByLanguage: {}", translatedByLanguage);
        log.info("enValues: {}", translatedByLanguage.get("en"));
        log.info("jaValues: {}", translatedByLanguage.get("ja"));

        List<String> enValues = translatedByLanguage.get("en");
        List<String> jaValues = translatedByLanguage.get("ja");

        if (!isValidTranslationChunk(enValues, sourceTexts.size())
                || !isValidTranslationChunk(jaValues, sourceTexts.size())) {
            log.warn("Invalid translation payload: {}", translatedByLanguage);
            throw new AppException(ErrorCode.TRANSLATION_FAILED);
        }

        System.out.println("enValues.get(0): "+ enValues.get(0));
        System.out.println("enValues.get(0): "+ enValues.get(0).getClass());

        product.setNameEn(enValues.get(0));
        product.setDescriptionEn(enValues.get(1));
        product.setBrandEn(enValues.get(2));

        product.setNameJa(jaValues.get(0));
        product.setDescriptionJa(jaValues.get(1));
        product.setBrandJa(jaValues.get(2));

        System.out.println("product: "+ product.getNameJa());
        productRepository.save(product);
    }

    private boolean isValidTranslationChunk(List<String> values, int expectedSize) {
        return values != null && values.size() >= expectedSize;
    }

    // @Override
    // @Transactional
    // public Product create(Product product, UUID categoryId) {
    // Category category = categoryRepository.findById(categoryId)
    // .orElseThrow(() -> new NotFoundException("Category not found: " +
    // categoryId));
    //
    // product.setId(null);
    // product.setCategory(category);
    //
    // return productRepository.save(product);
    // }

    // @Override
    // @Transactional
    // public Product update(UUID id, Product product, UUID categoryId) {
    // Product existing = getById(id);
    //
    // if (categoryId != null) {
    // Category category = categoryRepository.findById(categoryId)
    // .orElseThrow(() -> new NotFoundException("Category not found"));
    // existing.setCategory(category);
    // }
    //
    // if (product.getProductType() != null)
    // existing.setProductType(product.getProductType());
    // if (product.getName() != null) existing.setName(product.getName());
    // if (product.getDescription() != null)
    // existing.setDescription(product.getDescription());
    // if (product.getPrice() != null) existing.setPrice(product.getPrice());
    // if (product.getUnit() != null) existing.setUnit(product.getUnit());
    // if (product.getStatus() != null) existing.setStatus(product.getStatus());
    // if (product.getImageUrl() != null)
    // existing.setImageUrl(product.getImageUrl());
    //
    // return productRepository.save(existing);
    // }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0];

        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    // @Override
    // public Product uploadImage(UUID id, MultipartFile file) {
    // Product product = getById(id);
    //
    // try {
    // String uploadDir = System.getProperty("user.dir") + "/uploads/";
    // File dir = new File(uploadDir);
    //
    // if (!dir.exists()) dir.mkdirs();
    //
    // String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    // File dest = new File(uploadDir + fileName);
    //
    // file.transferTo(dest);
    //
    // product.setImageUrl("/uploads/" + fileName);
    //
    // return productRepository.save(product);
    //
    // } catch (IOException e) {
    // throw new RuntimeException("Upload failed");
    // }
    // }

    // @Override
    // public Product updateImage(UUID id, MultipartFile file) {
    //
    // Product product = getById(id);
    //
    // try {
    // String uploadDir = System.getProperty("user.dir") + "/uploads/";
    // File dir = new File(uploadDir);
    //
    // if (!dir.exists()) dir.mkdirs();
    //
    // if (product.getImageUrl() != null) {
    // String oldFileName = product.getImageUrl().replace("/uploads/", "");
    // File oldFile = new File(uploadDir + oldFileName);
    // if (oldFile.exists()) oldFile.delete();
    // }
    //
    // String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    // File dest = new File(uploadDir + fileName);
    //
    // file.transferTo(dest);
    //
    // product.setImageUrl("/uploads/" + fileName);
    //
    // return productRepository.save(product);
    //
    // } catch (IOException e) {
    // throw new RuntimeException("Update failed");
    // }
    // }
    // }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchByFranchise(UUID locationId, SearchProductRequest request) {
        List<UUID> variantIds = inventoryClient.getInStockVariantIds(locationId);
        if (variantIds == null || variantIds.isEmpty()) {
            return Page.empty();
        }

        String keyword = (request.getKeyword() != null && !request.getKeyword().trim().isEmpty())
                ? request.getKeyword().trim()
                : null;

        String categoryName = (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty())
                ? request.getCategoryName().trim()
                : null;

        ProductStatus status = ProductStatus.ACTIVE; // Optional for customers

        ProductColor color = (request.getColor() != null && !request.getColor().trim().isEmpty())
                ? ProductColor.valueOf(request.getColor().trim())
                : null;

        ProductSize size = (request.getSize() != null && !request.getSize().trim().isEmpty())
                ? ProductSize.valueOf(request.getSize().trim())
                : null;

        Pageable pageable = PageRequest.of(
                request.getPage().intValue(),
                request.getSizePage().intValue(),
                Sort.by(request.getSortBy()).ascending());

        return productRepository.searchByFranchise(
                keyword,
                categoryName,
                status,
                color,
                size,
                request.getFromPrice(),
                request.getToPrice(),
                variantIds,
                pageable);
    }


}