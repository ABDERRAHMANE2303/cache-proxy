package com.cache_proxy.models;


import org.springframework.http.MediaType;

public record ProxyResponse(
        int status,
        MediaType contentType,
        byte[] body
)
{}
