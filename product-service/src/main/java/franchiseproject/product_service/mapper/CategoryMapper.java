package franchiseproject.product_service.mapper;

import franchiseproject.product_service.dto.response.CategoryResponse;
import franchiseproject.product_service.entity.Category;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
    @Mapping(target = "lastUpdated", source = "updatedAt")
    CategoryResponse toCategoryResponse(Category category);
}
