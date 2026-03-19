package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.response.CategoryDetailResponse;
import franchiseproject.product_service.dto.request.CategoryRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.dto.response.ProductInCategoryResponse;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CategoryRequest request) {

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status("ACTIVE")
                .build();

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

//    @Override
//    public CategoryDetailResponse getById(UUID id) {
//
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Category not found"));
//
//        List<ProductInCategoryResponse> productResponses =
//                category.getProducts()
//                        .stream()
//                        .map(product -> ProductInCategoryResponse.builder()
//                                .name(product.getName())
//                                .price(product.getPrice())   // BigDecimal
//                                .build())
//                        .toList();
//
//        return CategoryDetailResponse.builder()
//                .id(category.getId())
//                .name(category.getName())
//                .description(category.getDescription())
//                .product(productResponses)
//                .build();
//    }

    @Override
    public CategoryResponse update(UUID id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category because it contains products");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        int productCount = category.getProducts() == null ? 0 : category.getProducts().size();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .productCount(productCount)
                .lastUpdated(category.getUpdatedAt())
                .build();
    }

}