package franchiseproject.product_service.mapper;

import franchiseproject.product_service.dto.response.ProductResponse;
import franchiseproject.product_service.dto.response.ProductVariantResponse;
import franchiseproject.product_service.entity.Product;
import franchiseproject.product_service.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ===== PRODUCT =====
    @Mapping(target = "variants", source = "variants")
    ProductResponse toProductResponse(Product product);

    // ===== VARIANT =====
    @Mapping(target = "status", source = "status")
    @Mapping(target = "price", source = "salePrice")
    @Mapping(target = "images", expression = "java(parseJsonToImageMap(productVariant.getImageUrl()))")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    // ===== LIST VARIANTS =====
    List<ProductVariantResponse> toProductVariantResponseList(List<ProductVariant> variants);

    default Map<String, String> parseJsonToImageMap(String json) {
        Map<String, String> imageMap = new HashMap<>();
        try {
            if (json == null || json.isEmpty()) {
                return imageMap;
            }

            // Parse chuỗi JSON array thành List
            List<String> urls = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, List.class);

            // Duyệt list và đánh index image01, image02...
            if (urls != null) {
                for (int i = 0; i < urls.size(); i++) {
                    // format: image01, image02... (cần %02d để có số 0 phía trước)
                    String key = String.format("image%02d", i + 1);
                    imageMap.put(key, urls.get(i));
                }
            }
        } catch (Exception e) {
            // Log error nếu cần thiết: log.error("Failed to parse images", e);
        }
        return imageMap;
    }
}