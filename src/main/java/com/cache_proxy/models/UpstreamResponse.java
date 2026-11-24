package com.cache_proxy.models;


import org.springframework.http.MediaType;

public record UpstreamResponse(
        int statusCode,
        MediaType contentType,
        byte[] body
)
{}
