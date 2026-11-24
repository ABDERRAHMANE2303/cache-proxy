package com.cache_proxy.controllers;


import com.cache_proxy.dtos.responses.CachedContentResponse;
import com.cache_proxy.dtos.responses.ProxyResponse;
import com.cache_proxy.models.ProxyResult;
import com.cache_proxy.services.ProxyService;
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
    public ResponseEntity<byte[]> proxyGet(
            HttpServletRequest request
    ){

        ProxyResponse response = proxyService.handleClientRequest(request);
        return ResponseEntity.status(response.proxyResult().status())
                .contentType(response.proxyResult().contentType())
                .header("X-Cache",response.status().name())
                .body(response.proxyResult().body());
    }

    @GetMapping("/cache/content")
    ResponseEntity<CachedContentResponse> cacheStatus(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(proxyService.cacheStatus());

    }
}
