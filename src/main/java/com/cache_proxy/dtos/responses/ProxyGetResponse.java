package com.cache_proxy.dtos.responses;

import com.cache_proxy.models.ProxyRequest;

public record ProxyGetResponse(
        String message,
        ProxyRequest parsedRequest
){}
