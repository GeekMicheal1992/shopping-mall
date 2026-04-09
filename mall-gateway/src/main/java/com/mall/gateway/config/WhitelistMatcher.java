package com.mall.gateway.config;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/*
    封装“路径是否白名单”的判断逻辑，避免在过滤器里写一堆匹配细节。
 */
@Component
public class WhitelistMatcher {
    private final WhitelistProperties whitelistProperties;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    public WhitelistMatcher(WhitelistProperties whitelistProperties) {
        this.whitelistProperties = whitelistProperties;
    }

    public boolean isWhitelisted(String path) {
       if (path == null || path.isBlank()) {
           return false;
        
       }
       if(whitelistProperties.getExact().stream().anyMatch(p -> p.equals(path))) {
           return true;
       }
       PathContainer pathContainer = PathContainer.parsePath(path);
        for (String patternText : whitelistProperties.getPrefix()) {
            PathPattern pattern = pathPatternParser.parse(patternText);
            if (pattern.matches(pathContainer)) {
                return true;
            }
        }

        return false;
    }
}
