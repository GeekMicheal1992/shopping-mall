package com.mall.product.service;

import com.mall.product.entity.Product;

public interface ProductCacheService {
    
    /**
     * 查缓存，没有就查数据库，然后写回缓存
     */
    Product getById(Long productId);
    
    /**
     * 写入缓存
     */
    void setCache(Product product);
    
    /**
     * 删除缓存
     */
    void deleteCache(Long productId);
}