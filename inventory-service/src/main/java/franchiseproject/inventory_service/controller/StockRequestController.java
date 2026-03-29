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
    public ApiResponse<List<StockRequestResponse>> getAllRequests(@RequestParam(required = false) UUID franchiseId) {
        List<StockRequestResponse> data = franchiseId != null 
                ? stockRequestService.getRequestsByFranchiseId(franchiseId) 
                : stockRequestService.getAllRequests();
                
        return ApiResponse.<List<StockRequestResponse>>builder()
                .statusCode(200)
                .message("Lấy danh sách yêu cầu nhập hàng thành công")
                .data(data)
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

    @PutMapping("/{id}/approve")
    public ApiResponse<StockRequestResponse> approveRequest(@PathVariable UUID id, @RequestParam UUID sourceLocationId, @RequestParam(required = false) UUID approvedBy) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Phê duyệt yêu cầu nhập hàng thành công")
                .data(stockRequestService.approveRequest(id, sourceLocationId, approvedBy))
                .build();
    }

    @PutMapping("/{id}/ship")
    public ApiResponse<StockRequestResponse> shipRequest(@PathVariable UUID id) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Xuất hàng thành công")
                .data(stockRequestService.shipRequest(id))
                .build();
    }

    @PutMapping("/{id}/receive")
    public ApiResponse<StockRequestResponse> receiveRequest(@PathVariable UUID id) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Nhận hàng thành công")
                .data(stockRequestService.receiveRequest(id))
                .build();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<StockRequestResponse> rejectRequest(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.<StockRequestResponse>builder()
                .statusCode(200)
                .message("Từ chối yêu cầu nhập hàng")
                .data(stockRequestService.rejectRequest(id, reason))
                .build();
    }
}
