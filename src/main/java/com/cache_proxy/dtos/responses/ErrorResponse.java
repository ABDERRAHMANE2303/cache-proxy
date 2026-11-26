package com.cache_proxy.dtos.responses;

public record ErrorResponse(int status, String error, String message) {}