package com.franchiseproject.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchiseproject.orderservice.dto.request.AddOnlineItemRequest;
import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.entity.PosCartItem;
import com.franchiseproject.orderservice.service.CartService;
import com.franchiseproject.orderservice.dto.response.CartItemResponse;
import com.franchiseproject.orderservice.client.ProductClient;
import com.franchiseproject.orderservice.client.InventoryClient;
import com.franchiseproject.orderservice.exception.AppException;
import com.franchiseproject.orderservice.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;
    ProductClient productClient;
    InventoryClient inventoryClient;

    @Override
    public void addPosItem(AddPosItemRequest request) {
        String key = "pos:cart:" + request.getTerminalId();
        String productField = request.getVariantId().toString();

        Object existingItem = redisTemplate
                .opsForHash()
                .get(key, productField);

        if (existingItem != null) {
            PosCartItem cartItem =
                    objectMapper.convertValue(existingItem, PosCartItem.class);
            cartItem.setQuantity(
                    cartItem.getQuantity() + request.getQuantity()
            );
            redisTemplate.opsForHash()
                    .put(key, productField, cartItem);
            redisTemplate.expire(key, Duration.ofMinutes(30));
            return;
        }
        PosCartItem newItem = PosCartItem.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .quantity(request.getQuantity())
                .build();
        redisTemplate.opsForHash()
                .put(key, productField, newItem);
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    @Override
    public void addOnlineItem(AddOnlineItemRequest request) {
        String key = "online:cart:" + request.getCustomerId();
        String productField = request.getVariantId().toString();

        Object existingItem = redisTemplate.opsForHash().get(key, productField);
        int currentQuantity = 0;
        if (existingItem != null) {
            PosCartItem cartItem = objectMapper.convertValue(existingItem, PosCartItem.class);
            currentQuantity = cartItem.getQuantity();
        }

        int newQuantity = currentQuantity + request.getQuantity();

        // Validate stock
        Map<UUID, Integer> availableStockMap = inventoryClient.getBulkAvailableStock(List.of(request.getVariantId()));
        int availableStock = availableStockMap.getOrDefault(request.getVariantId(), 0);

        if (newQuantity > availableStock) {
            throw new RuntimeException("Sản phẩm không đủ số lượng tồn kho (Còn lại: " + availableStock + ")");
        }

        if (existingItem != null) {
            PosCartItem cartItem = objectMapper.convertValue(existingItem, PosCartItem.class);
            cartItem.setQuantity(newQuantity);
            redisTemplate.opsForHash().put(key, productField, cartItem);
            return;
        }

        PosCartItem newItem = PosCartItem.builder()
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .quantity(request.getQuantity())
                .build();
        redisTemplate.opsForHash().put(key, productField, newItem);
    }

    @Override
    public List<PosCartItem> getCartPos(String terminalId) {
        String key = "pos:cart:" + terminalId;
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries(key);
        return entries.values().stream()
                .map(value -> objectMapper.convertValue(value, PosCartItem.class))
                .toList();
    }

    @Override
    public List<CartItemResponse> getCartOnline(UUID customerId) {
        String key = "online:cart:" + customerId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<PosCartItem> cartItems = entries.values().stream()
                .map(value -> objectMapper.convertValue(value, PosCartItem.class))
                .toList();

        List<UUID> variantIds = cartItems.stream()
                .map(PosCartItem::getVariantId)
                .toList();

        // Bulk fetch product details and stock
        Map<UUID, com.franchiseproject.orderservice.dto.response.ProductVariantDetailResponse> tempProductMap = Map.of();
        try {
            var apiProductResp = productClient.getProductVariantsBulk(variantIds);
            if (apiProductResp != null && apiProductResp.getData() != null) {
                tempProductMap = apiProductResp.getData().stream()
                        .collect(Collectors.toMap(
                                com.franchiseproject.orderservice.dto.response.ProductVariantDetailResponse::getId,
                                p -> p
                        ));
            }
        } catch (Exception e) {
            // handle gracefully or rethrow based on business needs
        }
        
        final Map<UUID, com.franchiseproject.orderservice.dto.response.ProductVariantDetailResponse> productMap = tempProductMap;

        Map<UUID, Integer> stockMap = inventoryClient.getBulkAvailableStock(variantIds);

        return cartItems.stream().map(item -> {
            var productDetail = productMap.get(item.getVariantId());
            int stock = stockMap.getOrDefault(item.getVariantId(), 0);

            return CartItemResponse.builder()
                    .productId(item.getProductId())
                    .variantId(item.getVariantId())
                    .quantity(item.getQuantity())
                    .productName(productDetail != null ? productDetail.getProductName() : "Unknown")
                    .size(productDetail != null ? productDetail.getSize() : "")
                    .color(productDetail != null ? productDetail.getColor() : "")
                    .price(productDetail != null ? productDetail.getPrice() : java.math.BigDecimal.ZERO)
                    .imageUrl(productDetail != null ? productDetail.getImageUrl() : "")
                    .stock(stock)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void updateOnlineItem(UUID customerId, UUID variantId, int newQuantity) {
        if (newQuantity <= 0) {
            removeOnlineItem(customerId, variantId);
            return;
        }

        Map<UUID, Integer> availableStockMap = inventoryClient.getBulkAvailableStock(List.of(variantId));
        int availableStock = availableStockMap.getOrDefault(variantId, 0);

        if (newQuantity > availableStock) {
            throw new RuntimeException("Sản phẩm không đủ số lượng tồn kho (Còn lại: " + availableStock + ")");
        }

        String key = "online:cart:" + customerId;
        String productField = variantId.toString();
        Object existingItem = redisTemplate.opsForHash().get(key, productField);
        if (existingItem != null) {
            PosCartItem cartItem = objectMapper.convertValue(existingItem, PosCartItem.class);
            cartItem.setQuantity(newQuantity);
            redisTemplate.opsForHash().put(key, productField, cartItem);
        }
    }

    @Override
    public void removePosItem(String terminalId, UUID variantId) {
        String key = "pos:cart:" + terminalId;
        String productField = variantId.toString();
        redisTemplate.opsForHash().delete(key, productField);
        Long size = redisTemplate.opsForHash().size(key);
        if (size != null && size == 0) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public void removeOnlineItem(UUID customerId, UUID variantId) {
        String key = "online:cart:" + customerId;
        String productField = variantId.toString();
        redisTemplate.opsForHash().delete(key, productField);
        Long size = redisTemplate.opsForHash().size(key);
        if (size != null && size == 0) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public void clearOnlineCart(UUID customerId) {
        String key = "online:cart:" + customerId;
        redisTemplate.delete(key);
    }

    @Override
    public void removeMultipleOnlineItems(UUID customerId, List<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;
        String key = "online:cart:" + customerId;
        Object[] fields = variantIds.stream().map(UUID::toString).toArray();
        redisTemplate.opsForHash().delete(key, fields);
        Long size = redisTemplate.opsForHash().size(key);
        if (size != null && size == 0) {
            redisTemplate.delete(key);
        }
    }


}
