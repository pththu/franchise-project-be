package franchiseproject.product_service.mapper;

import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
