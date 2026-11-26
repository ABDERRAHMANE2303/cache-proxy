package com.cache_proxy.exceptions.custom;

public class UpstreamTimeoutException extends RuntimeException {
    public UpstreamTimeoutException(Throwable cause) { super("Upstream timed out", cause); }
}


