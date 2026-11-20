package com.cache_proxy.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "cache-proxy")
public record CacheProxyParameters (Upstream upstream,@Valid @DefaultValue Cache cache) {
    public record Upstream(
            String baseUrl,
            @DefaultValue("2s") Duration connectionTimeout,
            @DefaultValue("5s") Duration readTimeout
    ){}
    public record Cache (
            @Min(1) @DefaultValue("1000") int capacity,
            @DefaultValue("60s") Duration ttl
    ){}
}
