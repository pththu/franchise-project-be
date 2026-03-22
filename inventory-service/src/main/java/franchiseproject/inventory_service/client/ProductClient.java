package franchiseproject.inventory_service.client;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.response.ProductVariantDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "product-service", url = "${application.feign.product-service.url:http://localhost:3001}")
public interface ProductClient {

    @GetMapping("/api/products/variant/{id}")
    ApiResponse<ProductVariantDetailResponse> getProductVariant(@PathVariable("id") java.util.UUID id);

    @PostMapping("/api/products/variants/bulk")
    ApiResponse<java.util.List<ProductVariantDetailResponse>> getProductVariantsBulk(@RequestBody java.util.List<java.util.UUID> ids);

}
