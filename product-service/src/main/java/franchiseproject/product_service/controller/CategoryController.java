package franchiseproject.product_service.controller;

import franchiseproject.product_service.dto.ApiResponse;
import franchiseproject.product_service.dto.request.CategoryCreationRequest;
import franchiseproject.product_service.dto.request.CategoryUpdateRequest;
import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;
import franchiseproject.product_service.enums.CategoryStatus;
import franchiseproject.product_service.exception.AppException;
import franchiseproject.product_service.exception.ErrorCode;
import franchiseproject.product_service.mapper.CategoryMapper;
import franchiseproject.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;
    CategoryMapper categoryMapper;

    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestBody @Valid CategoryCreationRequest request) {

        if (request.getName().isEmpty() || request.getName().equals("")) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }
        return ApiResponse.<CategoryResponse>builder()
                .statusCode(201)
                .message("Create category")
                .data(categoryMapper.toCategoryResponse(categoryService.create(request)))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .statusCode(200)
                .message("Get categories")
                .data(categoryService.getAll()
                        .stream()
                        .map(categoryMapper::toCategoryResponse)
                        .toList())
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable UUID id,
            @RequestBody CategoryUpdateRequest request
    ) {
        Category category = categoryService.getById(id);
        if (request.getName().isEmpty() || request.getName().equals("")) {
            throw new AppException(ErrorCode.DATA_IS_NULL);
        }

        return ApiResponse.<CategoryResponse>builder()
                .statusCode(200)
                .message("Category updated")
                .data(categoryMapper.toCategoryResponse(categoryService.update(category, request)))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable UUID id) {
        Category category = categoryService.getById(id);

        if (category.getStatus() == CategoryStatus.INACTIVE) {
            throw new AppException(ErrorCode.CATEGOTY_IS_DELETED);
        }

        return ApiResponse.<Boolean>builder()
                .statusCode(200)
                .message("Category deleted")
                .data(categoryService.delete(category))
                .build();
    }
}
