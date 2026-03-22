package franchiseproject.product_service.service.impl;

import franchiseproject.product_service.dto.request.CategoryCreationRequest;
import franchiseproject.product_service.dto.request.CategoryUpdateRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.enums.CategoryStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
import franchiseproject.product_service.repository.CategoryRepository;
import franchiseproject.product_service.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
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
    public Category update(Category category, CategoryUpdateRequest request) {
        if (category == null) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        log.info("request.getStatus(): {}", request.getStatus());

        if (request.getName() != null && !request.getName().equals("")) category.setName(request.getName());
        if (request.getDescription() != null && !request.getDescription().equals("")) category.setDescription(request.getDescription());
        if (request.getStatus() != null && !request.getStatus().equals("")) category.setStatus(CategoryStatus.valueOf(request.getStatus()));
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

        category.setStatus(CategoryStatus.INACTIVE);
        Category deleted = categoryRepository.save(category);
        return deleted == null ? false : true;
    }
}