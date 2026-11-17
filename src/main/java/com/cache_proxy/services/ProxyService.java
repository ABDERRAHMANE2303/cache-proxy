package com.cache_proxy.services;


import com.cache_proxy.dtos.responses.ProxyGetResponse;
import com.cache_proxy.models.ProxyRequest;
import org.springframework.stereotype.Service;

@Service
public class ProxyService {

    public ProxyGetResponse createRequestPOJO (String uri, String query){

        ProxyRequest proxyRequest = new ProxyRequest(uri.substring(7),query);
        return new ProxyGetResponse("POJO Created successfully",proxyRequest);
    }

}
