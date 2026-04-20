package com.mall.product.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mall.common.api.ApiResponse;
import com.mall.common.api.ApiResponses;
import com.mall.product.entity.Product;
import com.mall.product.service.ProductService;

import java.util.Map;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/list")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        Map<String, Object> result = productService.getProductList(keyword, categoryId, page, size);
        return ApiResponses.success(result);
    }

    
    @GetMapping("/product/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ApiResponse<Product> getProductById(@PathVariable(name = "id") Long id) {
        Product product = productService.getProductById(id);
        return ApiResponses.success(product);
    }

    
    @PostMapping("/product")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> create(@RequestBody Product product) {
        productService.createProduct(product);
        return ApiResponses.success();
    }

    
    @PutMapping("/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> update(
            @PathVariable(name = "id") Long id,
            @RequestBody Product product) {
        product.setId(id);
        productService.updateProduct(product);
        return ApiResponses.success();
    }

   
    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable(name = "id") Long id) {
        productService.deleteProduct(id);
        return ApiResponses.success();
    }

    @GetMapping("/internal/{id}")
    public Product getProductInternal(@PathVariable(name = "id") Long id) {
        return productService.getProductById(id);
}
}