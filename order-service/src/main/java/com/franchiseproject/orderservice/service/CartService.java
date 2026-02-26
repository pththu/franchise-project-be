package com.franchiseproject.orderservice.service;

import com.franchiseproject.orderservice.dto.request.AddPosItemRequest;
import com.franchiseproject.orderservice.model.PosCartItem;

import java.util.List;

public interface CartService {
  void addItem(AddPosItemRequest request);
  List<PosCartItem> getCartPos(String terminalId);
}
