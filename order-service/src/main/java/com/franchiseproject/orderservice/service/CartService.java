package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.request.AddOnlineItemRequest;
import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.model.PosCartItem;

import java.util.List;
import java.util.UUID;

public interface CartService {
  void addPosItem(AddPosItemRequest request);
  void addOnlineItem(AddOnlineItemRequest request);
  List<PosCartItem> getCartPos(String terminalId);
  List<PosCartItem> getCartOnline(UUID customerId);
  void removePosItem(String terminalId, UUID productId);
  void removeOnlineItem(UUID customerId, UUID productId);
}
