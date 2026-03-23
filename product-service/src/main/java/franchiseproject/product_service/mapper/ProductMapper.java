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

    // ===== PRODUCT =====
    @Mapping(target = "variants", source = "variants")
    ProductResponse toProductResponse(Product product);

    // ===== VARIANT =====
    @Mapping(target = "status", source = "status")
    @Mapping(target = "price", source = "salePrice")

    // 🔥 FIX QUAN TRỌNG NHẤT (JSON → LIST)
    @Mapping(target = "imageUrls",
            expression = "java(parseJsonToList(productVariant.getImageUrl()))")

    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    // ===== LIST VARIANTS =====
    List<ProductVariantResponse> toProductVariantResponseList(List<ProductVariant> variants);

    // ===== HELPER =====
    default List<String> parseJsonToList(String json) {
        try {
            if (json == null || json.isEmpty()) return List.of();
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }
}