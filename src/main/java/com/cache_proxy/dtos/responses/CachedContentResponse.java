package com.cache_proxy.dtos.responses;

import java.util.List;

public record CachedContentResponse(
        String baseUrl ,
        int capacity,
        int currentSize,
        List<Entry> cachedRequests
){
    public record Entry (int position , String clientRequest){}
}
