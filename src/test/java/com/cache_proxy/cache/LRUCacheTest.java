package com.cache_proxy.cache;

import static org.assertj.core.api.Assertions.*;
import com.cache_proxy.models.ClientRequest;
import com.cache_proxy.models.ProxyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.time.Duration;

public class LRUCacheTest {
    private LRUCache cache;
    private FakeClock fakeclock;

    @BeforeEach
    void setup (){
        fakeclock = new FakeClock();
        cache = new LRUCache(10,fakeclock,Duration.ofSeconds(60));
    }

    private LRUCache cacheWithCapacity(int capacity) {
        return new LRUCache(capacity, fakeclock, Duration.ofSeconds(60));
    }

    @Test
    @DisplayName("get returns empty when the key is not cached")
    void getReturnsEmptyForMissingKey(){
        String path = "/api/customers";
        String query = "status=INACTIVE";
        assertThat(cache.get(new ClientRequest(path,query))).isEmpty();
    }

    @Test
    @DisplayName("get returns the value stored by put")
    void getReturnsStoredValue(){
        ClientRequest req = request("request");
        ProxyResponse res = response(200);
        cache.put(req,res);
        assertThat(cache.get(req)).isEqualTo(Optional.of(res));
    }

    @Test
    @DisplayName("get moves the entry to most recently used")
    void getRefreshesRecency (){
        ClientRequest req1 = request("request1");
        ClientRequest req2 = request("req2");
        ProxyResponse res1 = response(200);
        ProxyResponse res2 = response(201);
        cache.put(req1,res1);
        cache.put(req2,res2);
        assertThat(cache.keysInOrder()).containsExactly(req1,req2);
        cache.get(req1);
        assertThat(cache.keysInOrder()).containsExactly(req2,req1);
    }

    @Test
    @DisplayName("put evicts the least recently used entry when the cache is full")
    // capacity = 2 -> put req1 req2 req3 -> cache contains req2 req3
    void putEvictsLeastRecentlyUsedEntryWhenFull(){
        cache = cacheWithCapacity(2);
        ClientRequest[] reqs = new ClientRequest[3];
        for (int i = 0;i<3;i++){
            reqs[i] = request("req" + i);
            cache.put(reqs[i],response("body" + i));
        }
        assertThat(cache.keysInOrder()).containsExactly(reqs[1],reqs[2]);
        assertThat(cache.get(reqs[0])).isEmpty();
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("put on an existing key replaces the value and refreshes recency without evicting")
    void putUpdatesExistingKeyWithoutEviction(){
        ClientRequest req1 = request("req1");
        ClientRequest req2 = request("req2");
        ProxyResponse res1 = response(200);
        ProxyResponse res2 = response(201);

        cache.put(req1,res1);
        cache.put(req2,res2);
        ProxyResponse tempres = response("temp");
        cache.put(req1, tempres);
        assertThat(cache.keysInOrder()).containsExactly(req2,req1);
        assertThat(cache.get(req1).orElseThrow()).isEqualTo(tempres);
    }

    @Test
    @DisplayName("entry is valid at exactly expiresAt and expired one millisecond later")
    void getEntryValidAtBoundaryAndExpiredJustAfter() {
        ClientRequest req = request("req");
        ProxyResponse res = response("response");
        cache.put(req, res);

        fakeclock.advance(Duration.ofSeconds(60));
        assertThat(cache.get(req)).isPresent();      // exactly at expiresAt: still valid

        fakeclock.advance(Duration.ofMillis(1));
        assertThat(cache.get(req)).isEmpty();        // 1 ms past: expired
    }

    @Test
    @DisplayName("put on an existing key resets the TTL")
    void putOnExistingKeyResetsTtl() {
        ClientRequest req = request("req");
        ProxyResponse res = response("response");
        cache.put(req, res);

        fakeclock.advance(Duration.ofSeconds(40));
        cache.put(req, res);
        fakeclock.advance(Duration.ofSeconds(40));
        assertThat(cache.get(req)).isPresent();
    }

    @Test
    @DisplayName("get on an expired entry removes it")
    void getExpiredEntryRemovesItFromCache() {
        ClientRequest req = request("req");
        ProxyResponse res = response("response");
        cache.put(req, res);

        fakeclock.advance(Duration.ofSeconds(61));
        cache.get(req);
        assertThat(cache.size()).isZero();
        assertThat(cache.keysInOrder()).isEmpty();
    }

    // a shorter req/res creator since i create them a lot , itll reduce so much boilerplate

    private static ClientRequest request(String name) {
        return new ClientRequest("/api/" + name, "q=" + name);
    }

    private static ProxyResponse response(String body) {
        return new ProxyResponse(200, MediaType.APPLICATION_JSON,
                body.getBytes(StandardCharsets.UTF_8));
    }

    private static ProxyResponse response(int status) {
        return new ProxyResponse(status, MediaType.APPLICATION_JSON,
                "{}".getBytes(StandardCharsets.UTF_8));
    }

}
