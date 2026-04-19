package com.mall.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;          
    private String orderNo;    
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private Integer status;     // 0待支付，1已支付，2已发货，3已完成，4已取消
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
