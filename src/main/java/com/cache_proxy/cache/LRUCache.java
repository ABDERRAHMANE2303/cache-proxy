package com.cache_proxy.cache;

import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResponse;

import java.time.Clock;
import java.time.Duration;
import java.util.*;


public class LRUCache {
    private final int capacity ;
    private final long ttl;
    private Map<ClientRequest, Node> requestNodeHashMap = new HashMap<>();
    private final Node head = new Node(),tail = new Node();
    private final Clock clock;

    public LRUCache(int capacity, Clock clock, Duration ttl) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
        this.clock = clock;
        this.ttl = ttl.toMillis();
        head.next = tail;
        tail.prev = head;
    }

    public void put(ClientRequest request ,ProxyResponse response ){
        long now = clock.millis();
        if (requestNodeHashMap.containsKey(request)) {
            Node node = requestNodeHashMap.get(request);
            node.response = response;
            remove(node);
            addLast(node);
            node.expiresAt = now + ttl;
        }else {
            Node node = new Node(request,response,now+ttl);
            if (requestNodeHashMap.size() == capacity){
                requestNodeHashMap.remove(head.next.request);
                remove(head.next);
            }
            addLast(node);
            requestNodeHashMap.put(request,node);
        }
    }

    public Optional<ProxyResponse> get (ClientRequest request){
        long now = clock.millis();
        Node responseNode = requestNodeHashMap.get(request);
        if (responseNode == null){
            return Optional.empty();
        }else if (responseNode.expiresAt < now) {
            remove(responseNode);
            requestNodeHashMap.remove(request);
            return Optional.empty();
        }
        remove(responseNode);
        addLast(responseNode);
        return Optional.of(responseNode.response);
    }

    public int size(){
        return requestNodeHashMap.size();
    }

    public List<ClientRequest> keysInOrder(){
        List<ClientRequest> KeysList = new ArrayList<>();
        Node curr = head.next;
        while (curr != tail){
            KeysList.add(curr.request);
            curr = curr.next;
        }
        return KeysList;
    }

    private class Node{
        ClientRequest request;
        ProxyResponse response;
        long expiresAt ;
        Node next;
        Node prev;
        public Node(
                ClientRequest request,
                ProxyResponse response,
                long expiresAt){
            this.request =request;
            this.response = response;
            this.expiresAt = expiresAt;
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
