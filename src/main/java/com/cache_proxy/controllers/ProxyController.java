package com.cache_proxy.controllers;


import com.cache_proxy.dtos.responses.CacheContentResponse;
import com.cache_proxy.models.ProxyResponse;
import com.cache_proxy.services.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController (ProxyService proxyService){
        this.proxyService = proxyService;
    }

    @GetMapping("/{*request}")
    public ResponseEntity<byte[]> proxyGet(
            HttpServletRequest request
    ){
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        ProxyResponse response = proxyService.createRequestPOJO(uri,query);
        return ResponseEntity.status(response.status())
                .contentType(response.contentType())
                .body(response.body());
    }

    @GetMapping("/cache/content")
    ResponseEntity<CacheContentResponse> cacheStatus(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(proxyService.cacheStatus());

    }
}
