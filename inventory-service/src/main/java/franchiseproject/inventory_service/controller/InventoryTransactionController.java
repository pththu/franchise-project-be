package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import franchiseproject.inventory_service.dto.response.PageResponse;
import franchiseproject.inventory_service.dto.response.InventoryTransactionResponse;
import franchiseproject.inventory_service.service.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/inventory/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionController {

    InventoryTransactionService service;

    @GetMapping
    public ApiResponse<PageResponse<InventoryTransactionResponse>> getTransactions(
            @RequestParam(required = false) java.util.UUID locationId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<InventoryTransactionResponse>>builder()
                .statusCode(200)
                .message("Lấy lịch sử giao dịch thành công")
                .data(service.getTransactions(locationId, from, to, page, size))
                .build();
    }
}
