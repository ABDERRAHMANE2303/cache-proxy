package com.cache_proxy.cache;

import com.cache_proxy.config.CacheProxyParameters;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LRUCache {
    private final CacheProxyParameters cachesettings;
    private final int capacity ;
    private Map<ClientRequest, Node> requestNodeHashMap = new HashMap<>();
    private final Node head = new Node(),tail = new Node();

    public LRUCache(CacheProxyParameters cachesettings){
        this.cachesettings = cachesettings;
        this.capacity = cachesettings.cache().capacity();
        head.next = tail;
        tail.prev = head;
    }

    public void put(ClientRequest request ,ProxyResponse response ){
        if (requestNodeHashMap.containsKey(request)) {
            Node node = requestNodeHashMap.get(request);
            node.response = response;
            remove(node);
            addLast(node);
        }else {
            Node node = new Node(request,response);
            if (requestNodeHashMap.size() == capacity){
                requestNodeHashMap.remove(head.next.request);
                remove(head.next);
                addLast(node);
            }else{
                addLast(node);
            }
            requestNodeHashMap.put(request,node);
        }
    }

    public Optional<ProxyResponse> get (ClientRequest request){
        if (!requestNodeHashMap.containsKey(request)){
            return Optional.empty();
        }
        remove(requestNodeHashMap.get(request));
        addLast(requestNodeHashMap.get(request));
        return Optional.of(requestNodeHashMap.get(request).response);
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
