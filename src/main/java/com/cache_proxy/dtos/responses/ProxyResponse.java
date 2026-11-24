package com.cache_proxy.dtos.responses;

import com.cache_proxy.enums.CacheStatus;
import com.cache_proxy.models.UpstreamResponse;

public record ProxyResponse(
        CacheStatus status,
        UpstreamResponse upstreamResponse
) {
}
