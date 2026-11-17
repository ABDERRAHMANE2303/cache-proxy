package com.cache_proxy.models;


public record ProxyRequest(
        String path,
        String query // convention : null if no query params are present
)
{}
