package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.request.AddOnlineItemRequest;
import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.entity.PosCartItem;

import java.util.List;
import java.util.UUID;

public interface CartService {
  void addPosItem(AddPosItemRequest request);
  void addOnlineItem(AddOnlineItemRequest request);
  List<PosCartItem> getCartPos(String terminalId);
  List<PosCartItem> getCartOnline(UUID customerId);
  void updateOnlineItem(UUID customerId, UUID variantId, int newQuantity);
  void removePosItem(String terminalId, UUID variantId);
  void removeOnlineItem(UUID customerId, UUID variantId);
}
