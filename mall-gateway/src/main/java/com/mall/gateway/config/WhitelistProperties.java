package com.mall.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
    从 application.yml 里读取 security.whitelist 配置，提供给 WhitelistMatcher 使用
 */
@Component
@ConfigurationProperties(prefix = "security.whitelist")
public class WhitelistProperties {
    
    /**
     * 精确匹配路径，比如 /auth/login
     */
    private List<String> exact = new ArrayList<>();
    /**
     * 前缀匹配路径，比如 /public/**
     */
    private List<String> prefix = new ArrayList<>();

    public List<String> getExact() {
        return exact;
    }

    public void setExact(List<String> exact) {
        this.exact = exact;
    }

    public List<String> getPrefix() {
        return prefix;
    }

    public void setPrefix(List<String> prefix) {
        this.prefix = prefix;
    }
}

