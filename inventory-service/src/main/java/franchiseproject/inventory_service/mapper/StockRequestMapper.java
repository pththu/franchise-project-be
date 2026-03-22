package franchiseproject.inventory_service.mapper;

import franchiseproject.inventory_service.dto.request.CreateStockRequest;
import franchiseproject.inventory_service.dto.request.StockRequestItemRequest;
import franchiseproject.inventory_service.dto.response.StockRequestItemResponse;
import franchiseproject.inventory_service.dto.response.StockRequestResponse;
import franchiseproject.inventory_service.entity.StockRequest;
import franchiseproject.inventory_service.entity.StockRequestItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockRequestMapper {
    @Mapping(target = "items", ignore = true)
    StockRequest toEntity(CreateStockRequest request);
    
    StockRequestResponse toResponse(StockRequest stockRequest);
    
    StockRequestItem toItemEntity(StockRequestItemRequest request);
    StockRequestItemResponse toItemResponse(StockRequestItem item);
}
