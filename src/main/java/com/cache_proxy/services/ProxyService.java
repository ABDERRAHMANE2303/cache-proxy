package com.cache_proxy.services;


import com.cache_proxy.cache.LRUCache;
import com.cache_proxy.config.CacheProxyParameters;
import com.cache_proxy.dtos.responses.CachedContentResponse;
import com.cache_proxy.dtos.responses.ProxyResponse;
import com.cache_proxy.enums.RequestStatus;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProxyService {

    private final CacheProxyParameters cacheProxyParameters;
    private final RestClient client;
    private final LRUCache cache;

    public ProxyService(
            CacheProxyParameters cacheProxyParameters,
            RestClient client,
            LRUCache cache
            ){
        this.client = client;
        this.cacheProxyParameters = cacheProxyParameters;
        this.cache = cache;
    }

    public ProxyResponse handleClientRequest(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String baseUrl = cacheProxyParameters.upstream().baseUrl();
        String prefix = "/proxy/";
        String remoteUri = uri.substring(prefix.length());

        ClientRequest clientRequest = new ClientRequest(remoteUri, query);

        // get from cache , if not call upstream , add to the cache and return the upstream call
        Optional<ProxyResult> cached = cache.get(clientRequest);
        if (cached.isPresent()) return new ProxyResponse(RequestStatus.HIT,cached.get());

        ProxyResult fresh = callUpstream(baseUrl,remoteUri,query);
        cache.put(clientRequest,fresh);
        return new ProxyResponse(RequestStatus.MISS,fresh);
    }

    public ProxyResult callUpstream(String baseUrl, String remoteUri, String query){
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

        return new ProxyResult(
                res.getStatusCode().value(),
                res.getHeaders().getContentType(),
                res.getBody());
    }

    public CachedContentResponse cacheStatus (){

        String baseUrl = cacheProxyParameters.upstream().baseUrl();
        int capacity = cacheProxyParameters.cache().capacity();
        int currentSize = cache.size();

        List<ClientRequest> requests = cache.keysInOrder();
        List<CachedContentResponse.Entry> cachedRequests = new ArrayList<>();
        for (int i=0 ; i< requests.size();i++) {
            cachedRequests.add(new CachedContentResponse.Entry(i,requests.get(i).toString()));
        }

        CachedContentResponse res = new CachedContentResponse(
                baseUrl,capacity,currentSize,cachedRequests);
        return res;
    }



}