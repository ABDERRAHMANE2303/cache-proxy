package com.cache_proxy.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache-proxy")
public record CacheProxyParameters (Upstream upstream) {
    public record Upstream(
            String baseUrl,
            @DefaultValue("2s")
            Duration connectionTimeout,
            @DefaultValue("5s")
            Duration readTimeout
    ){}
}
