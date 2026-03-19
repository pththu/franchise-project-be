package franchiseproject.product_service.service;

import franchiseproject.product_service.dto.response.CategoryDetailResponse;
import franchiseproject.product_service.dto.request.CategoryRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);

    List<CategoryResponse> getAll();

//    CategoryDetailResponse getById(UUID id);

    CategoryResponse update(UUID id, CategoryRequest request);

    void delete(UUID id);
}
