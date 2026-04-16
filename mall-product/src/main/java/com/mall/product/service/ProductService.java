package com.mall.product.service;

import com.mall.product.entity.Product;
import java.util.Map;

public interface ProductService {

    /**
     * 商品列表（分页 + 搜索）
     */
    Map<String, Object> getProductList(String keyword, Long categoryId, int page, int size);

    /**
     * 根据ID查询商品
     */
    Product getProductById(Long id);

    /**
     * 新增商品
     */
    void createProduct(Product product);

    /**
     * 更新商品
     */
    void updateProduct(Product product);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);
}