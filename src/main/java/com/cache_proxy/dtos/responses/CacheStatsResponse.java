package com.cache_proxy.dtos.responses;

public record CacheStatsResponse(
        long hits,
        long misses,
        long evictions,
        long expirations,
        int capacity,
        int size
) {}
