package com.mall.gateway.filter;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter  implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    @Override
    public int getOrder() {
        return -200;
    }

    /**
         * TraceId 过滤器
         * 
         * 作用：
         * 1. 为每个请求生成或透传唯一的 traceId
         * 2. 将 traceId 存入 MDC，让网关日志自动打印
         * 3. 将 traceId 添加到请求头，透传给下游服务
         * 
         * 原理：
         * - 实现 GlobalFilter 接口，成为全局过滤器
         * - 实现 Ordered 接口，设置 order = -200，确保最先执行
         * - 使用 MDC 存储 traceId，日志配置中通过 %X{traceId} 自动打印
         * - 通过 doFinally 确保请求完成后清理 MDC，防止线程复用导致 traceId 错乱
         * 
         * 执行顺序：-200（最高优先级，比 GatewayAuthFilter 的 -100 更早执行）
 */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if(traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }

        MDC.put("traceId", traceId);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();


        return chain.filter(mutatedExchange).doFinally(signalType->{
            MDC.remove("traceId");
        });
        
    }

    private String generateTraceId(){
        String timePart = String.valueOf(System.currentTimeMillis());
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return timePart + "-" + randomPart;
    }
    
}
