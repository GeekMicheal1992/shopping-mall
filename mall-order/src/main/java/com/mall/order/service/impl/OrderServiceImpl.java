package com.mall.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.mall.common.error.BizException;
import com.mall.common.error.ErrorCode;

import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.dto.ProductDto;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.service.OrderService;

import cn.hutool.core.util.IdUtil;

@Service
public class OrderServiceImpl implements OrderService {

    private final RestTemplate restTemplate;
    private final OrderMapper orderMapper;    
    public OrderServiceImpl(RestTemplate restTemplate, OrderMapper orderMapper) {
        this.restTemplate = restTemplate;
        this.orderMapper = orderMapper;
    }   
    @Override
    @Transactional
    public Long createOrder(CreateOrderRequest request) {
        if (request.getUserId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID,"用户ID不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID,"订单项不能为空");
        }

        Long orderId  = IdUtil.getSnowflakeNextId();
        String orderNo = String.valueOf(System.currentTimeMillis()) + 
                         (request.getUserId() % 10000); 
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemDto  itemDto : request.getItems()) {
        String productUrl = "http://localhost:8083/internal/" + itemDto.getProductId();
        ProductDto product = restTemplate.getForObject(productUrl, ProductDto.class);
            if (product == null) {
                throw new BizException(ErrorCode.PARAM_INVALID,"product is not found,ID:" + itemDto.getProductId());
            }
            if (product.getStock() == null || product.getStock() < itemDto.getQuantity()) {
                throw new BizException(ErrorCode.PARAM_INVALID,"product stock is not enough,ID:" + itemDto.getProductId());
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId); 
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setTotalPrice(itemTotal);
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);  // 0=待支付
        order.setRemark(request.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insertOrder(order);
        orderMapper.insertOrderItems(orderItems);
       
        return orderId;
    }

    @Override
    public Order getOrderById(Long id) {
        if (id == null || id <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        Order order = orderMapper.selectOrderById(id);
        if (order == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        return order;
    }

    @Override
    public Map<String, Object> getOrdersByUserId(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "用户ID不能为空");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<Order> orders = orderMapper.selectOrdersByUserId(userId, offset, size);
        int total = orderMapper.selectCountByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);           
        return result;
    }

    @Override
    public void cancelOrder(Long id) {
       if (id == null || id <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单ID不能为空");
        }
        Order order = orderMapper.selectOrderById(id);
        if (order == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单不存在");
        }
        if(order.getStatus() != 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单不能取消，当前状态：" + order.getStatus());
        }
        int result = orderMapper.updateOrderStatus(id, 4); // 4=已取消
        if (result == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "订单取消失败");
        }
    }
    
}
