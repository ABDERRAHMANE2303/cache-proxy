package com.cache_proxy.dtos.responses;

import com.cache_proxy.models.ClientRequest;

import java.util.List;

public record CacheContentResponse(
        List<String> cachedRequests
){}
