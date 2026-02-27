package com.franchiseproject.orderservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchiseproject.orderservice.dto.request.AddOnlineItemRequest;
import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.model.PosCartItem;
import com.franchiseproject.orderservice.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    @Override
    public void addPosItem(AddPosItemRequest request) {
        String key = "pos:cart:" + request.getTerminalId();
        String productField = request.getProductId().toString();

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
                .quantity(request.getQuantity())
                .build();
        redisTemplate.opsForHash()
                .put(key, productField, newItem);
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    @Override
    public void addOnlineItem(AddOnlineItemRequest request) {
        String key = "online:cart:" + request.getCustomerId();
        String productField = request.getProductId().toString();

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
            return;
        }
        PosCartItem newItem = PosCartItem.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
        redisTemplate.opsForHash()
                .put(key, productField, newItem);
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
    public List<PosCartItem> getCartOnline(UUID customerId) {
        String key = "online:cart:" + customerId;
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries(key);
        return entries.values().stream()
                .map(value -> objectMapper.convertValue(value, PosCartItem.class))
                .toList();
    }

    @Override
    public void removePosItem(String terminalId, UUID productId) {
        String key = "pos:cart:" + terminalId;
        String productField = productId.toString();
        redisTemplate.opsForHash().delete(key, productField);
        Long size = redisTemplate.opsForHash().size(key);
        if (size != null && size == 0) {
            redisTemplate.delete(key);
        }
    }

    @Override
    public void removeOnlineItem(UUID customerId, UUID productId) {
        String key = "online:cart:" + customerId;
        String productField = productId.toString();
        redisTemplate.opsForHash().delete(key, productField);
        Long size = redisTemplate.opsForHash().size(key);
        if (size != null && size == 0) {
            redisTemplate.delete(key);
        }
    }


}
