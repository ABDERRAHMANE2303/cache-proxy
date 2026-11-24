package com.cache_proxy.controllers;

import com.cache_proxy.dtos.responses.CacheStatsResponse;
import com.cache_proxy.dtos.responses.CachedContentResponse;
import com.cache_proxy.dtos.responses.ProxyResponse;
import com.cache_proxy.services.ProxyService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController (ProxyService proxyService){
        this.proxyService = proxyService;
    }

    @GetMapping("/proxy/{*request}")
    public ResponseEntity<byte[]> proxyGet(HttpServletRequest request){
        ProxyResponse response = proxyService.handleClientRequest(request);
        return ResponseEntity.status(response.upstreamResponse().statusCode())
                .contentType(response.upstreamResponse().contentType())
                .header("X-Cache",response.status().name())
                .body(response.upstreamResponse().body());
    }

    @GetMapping("/cache/content")
    ResponseEntity<CachedContentResponse> cacheContent(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(proxyService.cacheStatus());

    }

    @GetMapping("/cache/stats")
    ResponseEntity<CacheStatsResponse> cacheStats(ServletRequest servletRequest){
        CacheStatsResponse stats = proxyService.cacheStats();
        return ResponseEntity.status(HttpStatus.OK)
                .body(stats);
    }
}
