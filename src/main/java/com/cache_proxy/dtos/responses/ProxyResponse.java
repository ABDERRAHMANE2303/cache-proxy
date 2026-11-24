package com.cache_proxy.dtos.responses;

import com.cache_proxy.enums.RequestStatus;
import com.cache_proxy.models.ProxyResult;

public record ProxyResponse(
        RequestStatus status,
        ProxyResult proxyResult
) {
}
