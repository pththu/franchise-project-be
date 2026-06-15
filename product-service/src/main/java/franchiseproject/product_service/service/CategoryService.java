package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.request.CategoryCreationRequest;
import franchiseproject.product_service.dto.request.CategoryUpdateRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category create(CategoryCreationRequest request);

    List<Category> getAll();

    Category getById (UUID id);

    Category update(Category category, CategoryUpdateRequest request);

    boolean delete(Category category);
}
