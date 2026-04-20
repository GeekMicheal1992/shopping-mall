package com.mall.product.component;

import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.ProductCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CacheWarmer {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmer.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductBloomFilter productBloomFilter;

    @Autowired
    private ProductCacheService productCacheService;

    private final ExecutorService warmerPool = Executors.newFixedThreadPool(5);


    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmUp() {
        long startTime = System.currentTimeMillis();

        try {
            warmUpProductIds();
            warmUpHotProducts();
        } catch (Exception e) {
            log.error("failed to warm up cache", e);
        }

        long cost = System.currentTimeMillis() - startTime;
        log.info("warm up success, cost: {}ms", cost);
    }

    
    private void warmUpProductIds() {
    
        int pageSize = 5000;
        int pageNum = 1;
        int totalCount = 0;

        while (true) {
            int offset = (pageNum - 1) * pageSize;
            List<Long> productIds = productMapper.selectAllIds(offset, pageSize);

            if (productIds.isEmpty()) {
                break;
            }

            CountDownLatch latch = new CountDownLatch(productIds.size());

            for (Long productId : productIds) {
                warmerPool.submit(() -> {
                    try {
                        productBloomFilter.add(productId);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            totalCount += productIds.size();
            pageNum++;
            
            log.info("Already warmed up {} product IDs", totalCount);
        }

        log.info("Product IDs warm up completed, total: {}", totalCount);
    }

    
    private void warmUpHotProducts() {
       
        
        List<Product> hotProducts = productMapper.selectHotProducts(100);

        CountDownLatch latch = new CountDownLatch(hotProducts.size());

        for (Product product : hotProducts) {
            warmerPool.submit(() -> {
                try {
                    productCacheService.setCache(product);
                    log.debug("Warm up hot product: id={}, name={}", product.getId(), product.getName());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Hot products warm up completed, total: {}", hotProducts.size());
    }
}