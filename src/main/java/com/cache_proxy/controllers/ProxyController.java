package com.cache_proxy.controllers;


import com.cache_proxy.dtos.responses.ProxyStatus;
import com.cache_proxy.models.ProxyRequest;
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

    @GetMapping("/{path}")
    public ResponseEntity<ProxyStatus> getProxy(
            HttpServletRequest request
    ){
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        ProxyStatus response = proxyService.createPOJO(uri,query);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }


}
