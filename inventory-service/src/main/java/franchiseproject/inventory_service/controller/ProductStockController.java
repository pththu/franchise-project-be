package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.InitialStockRequest;
import franchiseproject.inventory_service.dto.request.StockRequestItemRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.ProductStockResponse;
import franchiseproject.inventory_service.service.ProductStockService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/stocks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductStockController {

    ProductStockService productStockService;

    @GetMapping
    public ApiResponse<PageResponse<ProductStockResponse>> getStocks(
            @RequestParam(required = false) UUID locationId,
            @RequestParam(defaultValue = "false") boolean lowStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String message = lowStock ? "Lấy danh sách hàng sắp hết thành công" : "Lấy danh sách tồn kho thành công";
        if (locationId != null) {
            message += " theo chi nhánh";
        }

        return ApiResponse.<PageResponse<ProductStockResponse>>builder()
                .statusCode(200)
                .message(message)
                .data(productStockService.getStocks(locationId, lowStock, page, size))
                .build();
    }

    @PostMapping("/receipt")
    public ApiResponse<Void> addInitialStock(@RequestBody InitialStockRequest request) {
        productStockService.addInitialStock(request);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Nhập kho ban đầu thành công")
                .build();
    }

    @GetMapping("/variants/in-stock")
    public ApiResponse<List<UUID>> getInStockVariantIds(@RequestParam UUID locationId) {
        return ApiResponse.<List<UUID>>builder()
                .statusCode(200)
                .message("Lấy danh sách mã biến thể còn hàng thành công")
                .data(productStockService.getInStockVariantIds(locationId))
                .build();
    }

    @PostMapping("/capable-branches")
    public ApiResponse<List<UUID>> getCapableBranches(@RequestBody List<StockRequestItemRequest> items) {
        return ApiResponse.<List<UUID>>builder()
                .statusCode(200)
                .message("Lấy danh sách chi nhánh có thể đáp ứng thành công")
                .data(productStockService.findCapableBranches(items))
                .build();
    }
    @PostMapping("/reserve")
    public ApiResponse<Void> reserveStock(@RequestBody List<StockRequestItemRequest> items, @RequestParam UUID locationId) {
        productStockService.reserveStock(items, locationId);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Trừ kho tạm thời (giữ hàng) thành công")
                .build();
    }

    @PostMapping("/release")
    public ApiResponse<Void> releaseStock(@RequestBody List<StockRequestItemRequest> items, @RequestParam UUID locationId) {
        productStockService.releaseStock(items, locationId);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Giải phóng kho tạm thời thành công")
                .build();
    }

    @PostMapping("/commit")
    public ApiResponse<Void> commitStock(@RequestBody List<StockRequestItemRequest> items, @RequestParam UUID locationId) {
        productStockService.commitStock(items, locationId);
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Khấu trừ kho chính thức thành công")
                .build();
    }
}
