package com.cache_proxy.models;


import org.springframework.http.MediaType;

public record ProxyResult(
        int status,
        MediaType contentType,
        byte[] body
)
{}
