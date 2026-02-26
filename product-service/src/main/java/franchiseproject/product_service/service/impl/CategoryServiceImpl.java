package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.CategoryDetailResponse;
import franchiseproject.product_service.dto.CategoryRequest;
import franchiseproject.product_service.dto.CategoryResponse;
import franchiseproject.product_service.dto.ProductInCategoryResponse;
import franchiseproject.product_service.model.Category;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.service.CategoryService;
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

    @Override
    public CategoryDetailResponse getById(UUID id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<ProductInCategoryResponse> productResponses =
                category.getProduct()
                        .stream()
                        .map(product -> ProductInCategoryResponse.builder()
                                .name(product.getName())
                                .price(product.getPrice())   // BigDecimal
                                .build())
                        .toList();

        return CategoryDetailResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .product(productResponses)
                .build();
    }

    @Override
    public CategoryResponse update(UUID id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

}