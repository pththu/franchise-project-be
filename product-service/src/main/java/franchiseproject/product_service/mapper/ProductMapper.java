package franchiseproject.product_service.mapper;

import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.dto.response.ProductVariantResponse;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {


    @Mapping(target = "variants", source = "variants")
    ProductResponse toProductResponse (Product product);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "price", source = "salePrice")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    List<ProductVariantResponse> toProductVariantResponseList(List<ProductVariant> variants);
}
