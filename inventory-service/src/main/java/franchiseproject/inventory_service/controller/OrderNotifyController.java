package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/notify")
@RequiredArgsConstructor
public class OrderNotifyController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/new-order/{franchiseId}")
    public ApiResponse<Void> notifyNewOrder(@PathVariable UUID franchiseId) {
        // Gửi thông báo tới kênh của Franchise
        messagingTemplate.convertAndSend("/topic/franchise/" + franchiseId + "/orders", "NEW_ORDER");
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Đã gửi thông báo đơn hàng mới")
                .build();
    }

    @PostMapping("/order-status/{orderId}")
    public ApiResponse<Void> notifyOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status,
            @RequestParam(required = false) UUID franchiseId) {
        // Gửi thông báo tới kênh của cá nhân đơn hàng (thường là mobile app/customer fe)
        messagingTemplate.convertAndSend("/topic/order/" + orderId, status);
        
        // Nếu có franchiseId, gửi tới kênh của Franchise để Staff Dashboard cập nhật list
        if (franchiseId != null) {
            // Chúng ta gửi "ORDER_UPDATED" để FE biết cần load lại list, 
            // hoặc gửi chính cái status đó nếu FE muốn xử lý chuyên sâu
            messagingTemplate.convertAndSend("/topic/franchise/" + franchiseId + "/orders", "ORDER_UPDATED");
        }
        
        return ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Đã gửi thông báo cập nhật trạng thái đơn hàng")
                .build();
    }
}
