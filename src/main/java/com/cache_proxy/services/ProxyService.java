package com.cache_proxy.services;


import com.cache_proxy.dtos.responses.ProxyStatus;
import com.cache_proxy.models.ProxyRequest;
import org.springframework.stereotype.Service;

@Service
public class ProxyService {

    public ProxyStatus createPOJO (String uri,String query){
        ProxyRequest proxyRequest = new ProxyRequest(uri,query);
        return new ProxyStatus ("POJO Created successfully");
    }

}
