package franchiseproject.inventory_service.mapper;

import franchiseproject.inventory_service.dto.response.ProductStockResponse;
import franchiseproject.inventory_service.entity.ProductStock;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductStockMapper {
    ProductStockResponse toResponse(ProductStock productStock);
}
