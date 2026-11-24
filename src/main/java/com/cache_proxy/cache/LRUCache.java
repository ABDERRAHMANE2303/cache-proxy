package com.cache_proxy.cache;

import com.cache_proxy.dtos.responses.CacheStatsResponse;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.UpstreamResponse;

import java.time.Clock;
import java.time.Duration;
import java.util.*;


public class LRUCache {
    private final int capacity ;
    private final long ttl;
    private final Map<ClientRequest, Node> requestNodeMap = new HashMap<>();
    private final Clock clock;
    private final Node head = new Node(),tail = new Node();
    private long hits =0 ;
    private long misses= 0;
    private long evictions =0 ;
    private long expirations =0;


    public LRUCache(int capacity, Clock clock, Duration ttl) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be positive");
        if (ttl == null || ttl.toMillis() <= 0)
            throw new IllegalArgumentException("ttl must be at least 1 ms");
        Objects.requireNonNull(clock);
        this.capacity = capacity;
        this.clock = clock;
        this.ttl = ttl.toMillis();
        head.next = tail;
        tail.prev = head;
    }

    public synchronized void put(ClientRequest request , UpstreamResponse response ){
        long now = clock.millis();
        Node requestedNode = requestNodeMap.get(request);
        if (requestedNode!=null) {
            requestedNode.response = response;
            unlink(requestedNode);
            addLast(requestedNode);
            requestedNode.expiresAt = now + ttl;
        }else {
            Node node = new Node(request,response,now+ttl);
            Node lru = head.next;
            if (requestNodeMap.size() == capacity){
                requestNodeMap.remove(lru.request);
                unlink(lru);
                if (lru.expiresAt < now) expirations++;
                else evictions++;
            }
            addLast(node);
            requestNodeMap.put(request,node);
        }
    }

    public synchronized Optional<UpstreamResponse> get (ClientRequest request){
        long now = clock.millis();
        Node responseNode = requestNodeMap.get(request);
        if (responseNode == null){
            misses++;
            return Optional.empty();
        }else if (responseNode.expiresAt < now) {
            expirations++;
            misses++;
            unlink(responseNode);
            requestNodeMap.remove(request);
            return Optional.empty();
        }
        unlink(responseNode);
        addLast(responseNode);
        hits ++;
        return Optional.of(responseNode.response);
    }

    public synchronized int size(){
        return requestNodeMap.size();
    }

    /** Returns keys ordered from least to most recently used. */
    public synchronized List<ClientRequest> keysInOrder(){
        List<ClientRequest> keysList = new ArrayList<>();
        Node curr = head.next;
        while (curr != tail){
            keysList.add(curr.request);
            curr = curr.next;
        }
        return keysList;
    }

    public CacheStatsResponse stats (){
        return new CacheStatsResponse(hits,misses,evictions,expirations,capacity,requestNodeMap.size());
    }

    // utilities

    private static class Node{
        ClientRequest request;
        UpstreamResponse response;
        long expiresAt ;
        Node next;
        Node prev;
        public Node(
                ClientRequest request,
                UpstreamResponse response,
                long expiresAt){
            this.request =request;
            this.response = response;
            this.expiresAt = expiresAt;
        }
        public Node(){}
    }

    private void unlink(Node node){
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
