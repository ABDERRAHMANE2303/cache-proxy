package com.cache_proxy.dtos.responses;

import com.cache_proxy.models.ClientRequest;

public record ProxyGetResponse(
        String message,
        String baseUrl,
        ClientRequest parsedRequest
){}
