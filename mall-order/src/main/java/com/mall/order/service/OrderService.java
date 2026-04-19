package com.mall.order.service;

import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import java.util.Map;

public interface OrderService {
    
    
    Long createOrder(CreateOrderRequest request);
    
   
    Order getOrderById(Long id);
    
   
    Map<String, Object> getOrdersByUserId(Long userId, int page, int size);
    
    
    void cancelOrder(Long id);
}