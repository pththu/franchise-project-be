package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.CreateStockTransferRequest;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.StockTransferResponse;
import franchiseproject.inventory_service.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/transfers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockTransferController {

    StockTransferService stockTransferService;

    @PostMapping
    public ApiResponse<StockTransferResponse> createTransfer(@Valid @RequestBody CreateStockTransferRequest request) {
        return ApiResponse.<StockTransferResponse>builder()
                .statusCode(200)
                .message("Tạo lệnh điều chuyển thành công")
                .data(stockTransferService.createTransfer(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<StockTransferResponse>> getAllTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) java.util.UUID fromLocationId) {

        return ApiResponse.<PageResponse<StockTransferResponse>>builder()
                .statusCode(200)
                .message("Lấy danh sách lệnh điều chuyển thành công")
                .data(stockTransferService.getAllTransfers(page, size, fromLocationId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StockTransferResponse> getTransferById(@PathVariable UUID id) {
        return ApiResponse.<StockTransferResponse>builder()
                .statusCode(200)
                .message("Lấy thông tin lệnh điều chuyển thành công")
                .data(stockTransferService.getTransferById(id))
                .build();
    }

    @PutMapping("/{id}/ship")
    public ApiResponse<StockTransferResponse> shipTransfer(@PathVariable UUID id) {
        return ApiResponse.<StockTransferResponse>builder()
                .statusCode(200)
                .message("Xuất kho thành công")
                .data(stockTransferService.shipTransfer(id))
                .build();
    }

    @PutMapping("/{id}/receive")
    public ApiResponse<StockTransferResponse> receiveTransfer(@PathVariable UUID id) {
        return ApiResponse.<StockTransferResponse>builder()
                .statusCode(200)
                .message("Nhận hàng thành công")
                .data(stockTransferService.receiveTransfer(id))
                .build();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<StockTransferResponse> rejectTransfer(@PathVariable UUID id) {
        return ApiResponse.<StockTransferResponse>builder()
                .statusCode(200)
                .message("Từ chối lệnh điều chuyển thành công")
                .data(stockTransferService.rejectTransfer(id))
                .build();
    }
}
