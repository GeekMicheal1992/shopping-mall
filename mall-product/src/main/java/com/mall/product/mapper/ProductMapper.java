package com.mall.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mall.product.entity.Product;

@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") Long id);

    List<Product> selectList(@Param("keyword") String keyword, 
                              @Param("categoryId") Long categoryId,
                              @Param("offset") int offset, 
                              @Param("limit") int limit);

    int selectCount(@Param("keyword") String keyword, 
                    @Param("categoryId") Long categoryId);

    int insert(Product product);
    int update(Product product);
    int deleteById(@Param("id") Long id);

    List<Long> selectAllIds(@Param("offset") int offset, @Param("limit") int limit);
    List<Product> selectHotProducts(@Param("limit") int limit);
}
