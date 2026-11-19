package com.cache_proxy.services;


import com.cache_proxy.config.CacheProxyParameters;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ProxyService {

    private final CacheProxyParameters cacheProxyParameters;
    private final RestClient client;
    public ProxyService(
            CacheProxyParameters cacheProxyParameters,
            RestClient client){
        this.client = client;
        this.cacheProxyParameters = cacheProxyParameters;
    }

    public ProxyResponse createRequestPOJO (String uri, String query){

        String baseUrl = cacheProxyParameters.upstream().baseUrl();
        String prefix = "/proxy/";
        String remoteUri = uri.substring(prefix.length());
        ClientRequest clientRequest = new ClientRequest(remoteUri,query);

        // a java.net.uri uri so it does not get decoded and corrupt the actual uri
        URI target = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/" + remoteUri)
                .query(query)
                .build(true)   // dont encode again , already encoded
                .toUri();

        ResponseEntity<byte[]> res = client.get()
                .uri(target)
                .retrieve()
                .toEntity(byte[].class);

        return new ProxyResponse(
                res.getStatusCode().value(),
                res.getHeaders().getContentType(),
                res.getBody());
    }
}