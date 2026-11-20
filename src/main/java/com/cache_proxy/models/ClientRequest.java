package com.cache_proxy.models;


public record ClientRequest(
        String path,
        String query // convention : null if no query params are present
){
    @Override
    public String toString(){
        return path + query != null ? query : ""  ;
    }
}
