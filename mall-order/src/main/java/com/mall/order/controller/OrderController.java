package com.mall.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mall.common.api.ApiResponse;
import com.mall.common.api.ApiResponses;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.service.OrderService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/order")
public class OrderController {
        private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ApiResponse<Long> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        Long orderId = orderService.createOrder(request);
        return ApiResponses.success(orderId);
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrder(@PathVariable(name = "id") Long id) {
        Order order = orderService.getOrderById(id);
        return ApiResponses.success(order);
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getOrdersByUserId( @RequestParam(name = "userId") Long userId,   
                                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                                              @RequestParam(name = "size", defaultValue = "10") int size) {
        Map<String, Object> orders = orderService.getOrdersByUserId(userId, page, size);
        return ApiResponses.success(orders);
    }

    @PutMapping("/cancel/{id}")
    public ApiResponse<Void> cancelOrder(@PathVariable(name = "id") Long id) {
        orderService.cancelOrder(id);
        return ApiResponses.success();
    }

}