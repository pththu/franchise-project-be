package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.request.CreateStockRequest;
import franchiseproject.inventory_service.dto.response.StockRequestResponse;
import franchiseproject.inventory_service.service.StockRequestService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockRequestController {

    StockRequestService stockRequestService;

    @PostMapping
    public ApiResponse<StockRequestResponse> createRequest(@Valid @RequestBody CreateStockRequest request) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Tạo và gửi yêu cầu nhập hàng thành công")
                .data(stockRequestService.createRequest(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StockRequestResponse>> getAllRequests() {
        return ApiResponse.<List<StockRequestResponse>>builder()
                .statusCode(200)
                .message("Lấy danh sách yêu cầu nhập hàng thành công")
                .data(stockRequestService.getAllRequests())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StockRequestResponse> getRequestById(@PathVariable UUID id) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Lấy thông tin yêu cầu nhập hàng thành công")
                .data(stockRequestService.getRequestById(id))
                .build();
    }
}
