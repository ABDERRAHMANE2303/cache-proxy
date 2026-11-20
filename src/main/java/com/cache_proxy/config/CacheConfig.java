package com.cache_proxy.config;

import com.cache_proxy.cache.LRUCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public LRUCache lruCache(CacheProxyParameters cachesettings){
        return new LRUCache(cachesettings.cache().capacity());
    }

}
