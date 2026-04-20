package com.mall.product.component;

import javax.annotation.PostConstruct;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductBloomFilter {

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<Long> bloomFilter;

    private static final String BLOOM_FILTER_NAME = "product:bloom";

    private static final long EXPECTED_INSERTIONS = 1_000_000L;
    private static final double FALSE_PROBABILITY = 0.0001d;
    private static final Logger log = LoggerFactory.getLogger(ProductBloomFilter.class);

    @PostConstruct
    public void init() {
        this.bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_NAME);
         boolean initialized = bloomFilter.tryInit(EXPECTED_INSERTIONS, FALSE_PROBABILITY);
        if (initialized) {
            log.info("Bloom Filter initialized successfully.");
        } else {    
            log.info(" Bloom Filter is already initialized.");
        }
    }

    public void add(Long productId) {
        if (productId == null || productId <= 0) {
             return;
            
        }
        bloomFilter.add(productId);
    }

    public long getCount() {
        return bloomFilter.count();
    }

    public boolean mightContain(Long productId) {
        if (productId == null || productId <= 0) {
            return false;
        }
        return bloomFilter.contains(productId);
    }
    public void delete() {
        bloomFilter.delete();
    }

    public long getExpectedInsertions() {
        return  bloomFilter.getExpectedInsertions();
    }

    public double getFalseProbability() {
        return bloomFilter.getFalseProbability();
    }
    
}
