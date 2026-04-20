package com.mall.product.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.mall.product.component.ProductBloomFilter;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.ProductCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCacheServiceImpl implements ProductCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductBloomFilter productBloomFilter;

    private static final String CACHE_PREFIX = "product:cache:";
    private static final String LOCK_PREFIX = "product:lock:";
    private static final String NULL_VALUE = "NULL";
    private static final Random RANDOM = new Random();

    @Override
    public Product getById(Long productId) {
        if (!productBloomFilter.mightContain(productId)) {
            // 连 Redis 都不查，直接返回
            return null;
        }
        String cacheKey = CACHE_PREFIX + productId;
        
        // 1. 查 Redis
        String cachedData = redisTemplate.opsForValue().get(cacheKey);
        
        if (StrUtil.isNotBlank(cachedData)) {
            // 2. 判断是否为空值标记（防穿透）
            if (NULL_VALUE.equals(cachedData)) {
                return null;
            }
            // 3. 正常数据，反序列化返回
            return JSONUtil.toBean(cachedData, Product.class);  
        }
        
        // 4. 缓存未命中，加锁查数据库
        return getProductWithLock(productId, cacheKey);
    }

    private Product getProductWithLock(Long productId, String cacheKey) {
        String lockKey = LOCK_PREFIX + productId;
        Product product = null;
        boolean locked = false;
        
        try {
            locked = tryLock(lockKey, 10);
            
            if (locked) {
                // 双重检查
                String cachedData = redisTemplate.opsForValue().get(cacheKey);
                if (StrUtil.isNotBlank(cachedData)) {
                    if (NULL_VALUE.equals(cachedData)) {
                        return null;
                    }
                    return JSONUtil.toBean(cachedData, Product.class);
                }
                
                // 查数据库
                product = productMapper.selectById(productId);
                
                // 写回缓存
                if (product != null) {
                    setCache(product);
                } else {
                    setNullCache(cacheKey);
                }
            } else {
                Thread.sleep(50);
                return getById(productId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                unlock(lockKey);
            }
        }
        
        return product;
    }

    @Override
    public void setCache(Product product) {
        if (product == null || product.getId() == null) {
            return;
        }
        String cacheKey = CACHE_PREFIX + product.getId();
        long ttl = 30 + RANDOM.nextInt(10);  
        redisTemplate.opsForValue().set(
                cacheKey,
                JSONUtil.toJsonStr(product),  
                ttl,
                TimeUnit.MINUTES
        );
    }

    private void setNullCache(String cacheKey) {
        redisTemplate.opsForValue().set(cacheKey, NULL_VALUE, 2, TimeUnit.MINUTES);
    }

    @Override
    public void deleteCache(Long productId) {
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
    }

    
    private boolean tryLock(String key, long timeoutSeconds) {
        String value = String.valueOf(System.currentTimeMillis() + timeoutSeconds * 1000);
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, timeoutSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    private void unlock(String key) {
        redisTemplate.delete(key);
    }
}