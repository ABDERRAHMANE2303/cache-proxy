package com.cache_proxy.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
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
