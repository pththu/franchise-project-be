package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.request.CategoryCreationRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.enums.CategoryStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
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
    public Category create(CategoryCreationRequest request) {

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(CategoryStatus.ACTIVE)
                .build();

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGOTY_NOT_FOUND));
    }

    @Override
    public Category update(Category category, CategoryCreationRequest request) {
        if (category == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        if (!request.getName().equals("")) category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(CategoryStatus.valueOf(request.getStatus()));
        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public boolean delete(Category category) {

        if (category == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new AppException(ErrorCode.CONTAINS_PRODUCTS);
        }

        categoryRepository.delete(category);
        return true;
    }
}