package com.cache_proxy.cache;

import com.cache_proxy.config.CacheProxyParameters;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LRUCache {
    private final CacheProxyParameters cachesettings;
    private final int capacity ;
    private Map<ClientRequest, Node> cache = new HashMap<>();
    private final Node head = new Node(),tail = new Node();

    public LRUCache(CacheProxyParameters cachesettings){
        this.cachesettings = cachesettings;
        this.capacity = cachesettings.cache().capacity();
        head.next = tail;
        tail.prev = head;
    }

    public void put(ClientRequest request ,ProxyResponse response ){
        if (cache.containsKey(request)) {
            Node node = cache.get(request);
            node.response = response;
            remove(node);
            addLast(node);
        }else {
            Node node = new Node(request,response);
            if (cache.size() == capacity){
                cache.remove(head.next.request);
                remove(head.next);
                addLast(node);
            }else{
                addLast(node);
            }
            cache.put(request,node);
        }
    }

    public Optional<ProxyResponse> get (ClientRequest request){
        if (!cache.containsKey(request)){
            return Optional.empty();
        }
        remove(cache.get(request));
        addLast(cache.get(request));
        return Optional.of(cache.get(request).response);
    }

    private class Node{
        ClientRequest request;
        ProxyResponse response;
        Node next;
        Node prev;
        public Node(ClientRequest request, ProxyResponse response){
            this.request =request;
            this.response = response;
        }
        public Node(){}
    }

    private void remove (Node node){
        node.prev.next = node.next;
        node.next.prev=node.prev;
    }

    private void addLast(Node node){
        node.next = tail;
        node.prev = tail.prev;
        tail.prev.next = node;
        tail.prev = node;
    }
}
