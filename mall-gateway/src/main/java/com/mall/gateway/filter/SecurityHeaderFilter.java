package com.mall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class SecurityHeaderFilter implements GlobalFilter, Ordered {
   

    @Override
    public int getOrder() {
        return -50; // 确保在其他过滤器之前执行
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO Auto-generated method stub
        HttpHeaders  headers = exchange.getResponse().getHeaders();
        headers.set("X-XSS-Protection", "1; mode=block"); 
        headers.set("X-Frame-Options", "DENY"); 
        headers.set("X-Content-Type-Options", "nosniff"); 
        headers.remove("X-Powered-By");
        return chain.filter(exchange);
    }
    
}
