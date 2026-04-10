package com.mall.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Mono;

/*
    请求耗时（哪个接口慢？）

    客户端 IP（谁在请求？）

    最终状态码（成功还是失败？）

    响应结果（报了什么错？）
*/

@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 记录请求日志
        long startTime = System.currentTimeMillis();
        ServerHttpRequest  request = exchange.getRequest();
        log.info("--> {} {} | IP: {}", request.getMethod(), request.getURI().getPath(),getClientIp(request));

        ServerWebExchange decoratedExchange = new ServerWebExchangeDecorator(exchange) {
            @Override
            public ServerHttpResponse getResponse() {
                ServerHttpResponse response = super.getResponse();
                response.beforeCommit(() -> { //beforeCommit在响应真正提交给客户端之前执行回调
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("<--{} {} | Duration: {} ms", request.getMethod(), request.getURI().getPath(), duration);
                    return Mono.empty();
                });
                return response;
            }
        };
          
        return chain.filter(decoratedExchange);
    }

    private String  getClientIp(ServerHttpRequest request) {

       String ip = request.getHeaders().getFirst("X-Forwarded-For");
       if (ip   == null || ip.isEmpty()) {
           ip = request.getHeaders().getFirst("X-Real-IP");
       } 
       if(ip == null || ip.isEmpty()) {
          ip = request.getRemoteAddress().getAddress().getHostAddress();
       }
         return ip;
    }

    @Override
    public int getOrder() {
        return -150; // 设置过滤器顺序，数值越小优先级越高
    }
    
}
