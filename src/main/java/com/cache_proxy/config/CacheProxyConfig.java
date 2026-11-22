package com.cache_proxy.config;

import com.cache_proxy.cache.LRUCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration
public class CacheProxyConfig {

    @Bean
    public LRUCache lruCache(CacheProxyParameters cachesettings){
        return new LRUCache(cachesettings.cache().capacity(),Clock.systemUTC(),cachesettings.cache().ttl());
    }

    @Bean
    public RestClient restClient (CacheProxyParameters parameters){

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(parameters.upstream().connectionTimeout());
        factory.setReadTimeout(parameters.upstream().readTimeout());
        return RestClient.builder()
                .baseUrl(parameters.upstream().baseUrl())
                .requestFactory(factory)
                .build();
    }

}
