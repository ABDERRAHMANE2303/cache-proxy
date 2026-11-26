package com.cache_proxy.exceptions.custom;

public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(Throwable cause) { super("Upstream unreachable", cause); }
}
