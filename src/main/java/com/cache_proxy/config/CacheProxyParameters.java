package com.cache_proxy.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache-proxy")
public record CacheProxyParameters (Uptream upstream) {
    public record Uptream(String baseUrl){}
}
