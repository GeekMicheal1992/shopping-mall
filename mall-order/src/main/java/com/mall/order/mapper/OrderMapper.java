package com.mall.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;

@Mapper
public interface OrderMapper {  

    void insertOrder(Order order);
    
    void insertOrderItems(List<OrderItem> items);
    
    Order selectOrderById(@Param("id") Long id);
    
    List<Order> selectOrdersByUserId(@Param("userId") Long userId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);
    
    int selectCountByUserId(@Param("userId") Long userId);
    
   
    int updateOrderStatus(@Param("id") Long id, @Param("status") Integer status);
}