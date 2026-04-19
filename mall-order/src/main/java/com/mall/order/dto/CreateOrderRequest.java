package com.mall.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDto> items;
    
    private String remark;
    
    @Data
    public static class OrderItemDto {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;
    }
}