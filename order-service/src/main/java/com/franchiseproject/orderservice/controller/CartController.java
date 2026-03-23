package com.franchiseproject.orderservice.controller;

import com.franchiseproject.orderservice.dto.request.AddOnlineItemRequest;
import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.dto.response.ApiResponse;
import com.franchiseproject.orderservice.entity.PosCartItem;
import com.franchiseproject.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/carts")
public class CartController {
    CartService cartService;

    // Thêm sản phẩm vào giỏ hàng (POS)
    @PostMapping("/pos/add")
    public ApiResponse<Void> addItemToPosCart(
            @Valid @RequestBody AddPosItemRequest request) {
        cartService.addPosItem(request);
        return ApiResponse.<Void>builder()
                .message("Thêm sản phẩm vào giỏ thành công")
                .data(null)
                .statusCode(200)
                .errors(null)
                .build();
    }

    // Lấy danh sách sản phẩm trong giỏ hàng (POS)
    @GetMapping("/pos/{terminalId}")
    public ApiResponse<List<PosCartItem>> getPosCart(
            @PathVariable String terminalId) {
        return ApiResponse.<List<PosCartItem>>builder()
                .message("Lấy giỏ hàng thành công")
                .data(cartService.getCartPos(terminalId))
                .statusCode(200)
                .errors(null)
                .build();
    }

    // Thêm sản phẩm vào giỏ hàng (Online)
    @PostMapping("/online/add")
    public ApiResponse<Void> addItemToOnlineCart(
            @Valid @RequestBody AddOnlineItemRequest request) {
        cartService.addOnlineItem(request);
        return ApiResponse.<Void>builder()
                .message("Thêm sản phẩm vào giỏ Online thành công")
                .data(null)
                .statusCode(200)
                .errors(null)
                .build();
    }
    // Lấy danh sách sản phẩm trong giỏ hàng (Online)
    @GetMapping("/online/{customerId}")
    public ApiResponse<List<PosCartItem>> getOnlineCart(
            @PathVariable UUID customerId) {
        return ApiResponse.<List<PosCartItem>>builder()
                .message("Lấy giỏ Online thành công")
                .data(cartService.getCartOnline(customerId))
                .statusCode(200)
                .errors(null)
                .build();
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng (Online)
    @PutMapping("/online/{customerId}/update/{variantId}")
    public ApiResponse<Void> updateOnlineItem(
            @PathVariable UUID customerId,
            @PathVariable UUID variantId,
            @RequestParam int quantity) {
        cartService.updateOnlineItem(customerId, variantId, quantity);
        return ApiResponse.<Void>builder()
                .message("Cập nhật số lượng Online thành công")
                .statusCode(200)
                .data(null)
                .build();
    }

    // Xóa 1 sản phẩm trong giỏ hàng (POS)
    @DeleteMapping("/pos/{terminalId}/remove/{variantId}")
    public ApiResponse<Void> removePosItem(
            @PathVariable String terminalId,
            @PathVariable UUID variantId) {
        cartService.removePosItem(terminalId, variantId);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm khỏi giỏ POS thành công")
                .statusCode(200)
                .data(null)
                .errors(null)
                .build();
    }

    //Xóa 1 sản phẩm trong giỏ hàng (Online)
    @DeleteMapping("/online/{customerId}/remove/{variantId}")
    public ApiResponse<Void> removeOnlineItem(
            @PathVariable UUID customerId,
            @PathVariable UUID variantId) {
        cartService.removeOnlineItem(customerId, variantId);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm khỏi giỏ Online thành công")
                .statusCode(200)
                .data(null)
                .errors(null)
                .build();
    }
}