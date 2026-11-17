package com.cache_proxy.services;


import com.cache_proxy.config.CacheProxyParameters;
import com.cache_proxy.dtos.responses.ProxyGetResponse;
import com.cache_proxy.models.ProxyRequest;
import org.springframework.stereotype.Service;

@Service
public class ProxyService {

    public final CacheProxyParameters cacheProxyParameters;
    public ProxyService(CacheProxyParameters cacheProxyParameters){
        this.cacheProxyParameters = cacheProxyParameters;
    }

    public ProxyGetResponse createRequestPOJO (String uri, String query){
        String baseUrl = cacheProxyParameters.upstream().baseUrl();
        String prefix = "/proxy/";
        ProxyRequest proxyRequest = new ProxyRequest(uri.substring(prefix.length()),query);
        return new ProxyGetResponse("POJO Created successfully",baseUrl,proxyRequest);
    }
}