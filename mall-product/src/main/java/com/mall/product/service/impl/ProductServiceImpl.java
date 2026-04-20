package com.mall.product.service.impl;

import com.mall.common.error.BizException;
import com.mall.common.error.ErrorCode;
import com.mall.product.component.ProductBloomFilter;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.ProductCacheService;
import com.mall.product.service.ProductService;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final ProductBloomFilter productBloomFilter;

    public ProductServiceImpl(ProductMapper productMapper, ProductCacheService productCacheService, ProductBloomFilter productBloomFilter) {
        this.productMapper = productMapper;
        this.productCacheService = productCacheService;
        this.productBloomFilter = productBloomFilter;
    }

    @Override
    public Map<String, Object> getProductList(String keyword, Long categoryId, int page, int size) {
        // 参数处理
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;

        // 查询数据
        int offset = (page - 1) * size;
        List<Product> list = productMapper.selectList(keyword, categoryId, offset, size);
        int total = productMapper.selectCount(keyword, categoryId);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public Product getProductById(Long id) {
        
        if (id == null || id <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID不能为空");
        }
       
        Product product = productCacheService.getById(id);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    @Override
    public void createProduct(Product product) {
        // 参数校验
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品价格必须为正数");
        }
        // 设置默认值
        if (product.getStock() == null || product.getStock() < 0) {
            product.setStock(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        // 保存商品
        productMapper.insert(product);
        productCacheService.setCache(product);
        productBloomFilter.add(product.getId());
    }

    @Override
    public void updateProduct(Product product) {
        // 参数校验
        if (product.getId() == null || product.getId() <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID不能为空");
        }
        // 检查商品是否存在
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        // 只更新传入的非空字段
        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getPrice() != null) {
            existing.setPrice(product.getPrice());
        }
        if (product.getStock() != null) {
            existing.setStock(product.getStock());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        if (product.getImageUrl() != null) {
            existing.setImageUrl(product.getImageUrl());
        }
        if (product.getStatus() != null) {
            existing.setStatus(product.getStatus());
        }
        if (product.getSort() != null) {
            existing.setSort(product.getSort());
        }
        // 更新商品
        productMapper.update(existing);
        productCacheService.deleteCache(product.getId());
    }

    @Override
    public void deleteProduct(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID不能为空");
        }
        // 检查商品是否存在
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        // 删除商品
        productMapper.deleteById(id);
        productCacheService.deleteCache(id);
    }
}