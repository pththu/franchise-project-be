package franchiseproject.inventory_service.mapper;

import franchiseproject.inventory_service.dto.response.ProductFranchiseResponse;
import franchiseproject.inventory_service.entity.ProductFranchise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductFranchiseMapper {

    ProductFranchiseResponse toProductFranchiseResponse(ProductFranchise productFranchise);
}
