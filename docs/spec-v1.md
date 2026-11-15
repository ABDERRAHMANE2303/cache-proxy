# cache-proxy — Requirements Specification
  
## 1. Purpose and scope
 
cache-proxy is an HTTP service that sits in front of a single configurable upstream API. It forwards `GET` requests to the upstream, stores successful responses in a bounded in-memory cache with least-recently-used (LRU) eviction and time-based expiry (TTL/Time To Live), limits the request rate per client, and reports operating statistics.
 
The service is domain-agnostic. It has no knowledge of what the upstream API means, and pointing it at a different upstream requires a configuration change only.
  
## 2. Terminology
 
| Term | Meaning |
| --- | --- |
| Upstream | The HTTP API the service forwards requests to |
| Client | The caller of the service, as identified for rate limiting |
| Entry | A stored upstream response together with the data needed to serve and expire it |
| Hit | A request answered from a valid entry without contacting the upstream |
| Miss | A cacheable request for which no valid entry exists, so the upstream is contacted |
| TTL | Maximum age of an entry before it is no longer valid |
| Capacity | Maximum number of entries held at one time |
| Window | The time interval over which the rate limit is counted |
 
## 3. Implementation constraints
 
| ID | Constraint |
| --- | --- |
| IC‑1 | The cache MUST be implemented within the project. Third-party or framework caching facilities (Caffeine, Guava, Spring Cache) and the eviction mechanism of an access-ordered `LinkedHashMap` MUST NOT be used. |
| IC-2 | The rate limiter MUST be implemented within the project. Third-party rate-limiting libraries (Bucket4j, Resilience4j) MUST NOT be used. |
| IC-3 | The service MUST be built with Spring Boot. Standard library, Spring and testing libraries MAY be used. |
| IC-4 | Automated tests MUST NOT depend on network access or on a real upstream. |
| IC-5 | Automated tests MUST NOT wait in real time for a TTL to expire or for a rate-limit window to pass. |
 
## 4. Configuration
 
| ID | Requirement |
| --- | --- |
| CFG‑1 | The parameters below MUST be configurable through `application.yml`. Property names are implementation-defined. |
| CFG-2 | Every parameter except the upstream base URL MUST have a default value, so that the service starts when the parameter is omitted. |
 
| Parameter | Description |
| --- | --- |
| Upstream base URL | Location requests are forwarded to |
| Cache capacity | Maximum number of entries |
| Cache TTL | Maximum age of an entry |
| Rate limit | Maximum requests per client per window, and the window length |
| Upstream timeouts | Connect timeout and read timeout |
 
## 5. Interfaces
 
### 5.1 Proxy endpoint
 
```
GET /proxy/{path}?{query}
```
 
| ID | Requirement |
| --- | --- |
| PX‑1 | The service MUST accept `GET` requests on `/proxy/**`, including nested path segments and an optional query string. |
| PX-2 | A request to `/proxy/<path>?<query>` MUST be forwarded to `<upstream base URL>/<path>?<query>`. |
| PX-3 | Except for the cases defined in sections 7 and 8, the status, body and content type returned to the client MUST be those received from the upstream. |
| PX-4 | Methods other than `GET` on `/proxy/**` MUST be rejected with status `405`. |
| PX-5 | Every response produced from a cache lookup or an upstream call MUST carry the header `X-Cache` with the value `HIT` or `MISS`. |
 
### 5.2 Stats endpoint
 
```
GET /stats
```
 
| ID | Requirement |
| --- | --- |
| ST‑1 | The service MUST return a JSON document containing at least the fields listed below. Field names are implementation-defined. |
| ST-2 | The sum of hits and misses MUST equal the number of `GET` requests on `/proxy/**` admitted by the rate limiter. |
 
| Field | Meaning |
| --- | --- |
| Hits | Requests answered from the cache |
| Misses | Requests that required an upstream call |
| Evictions | Entries removed because capacity was reached |
| Expirations | Entries found expired by TTL |
| Size | Current number of entries |
| Capacity | Configured capacity |
| Rejected | Requests rejected by the rate limiter |
 
## 6. Cache behavior
 
| ID | Requirement |
| --- | --- |
| CACHE-1 | Only responses to `GET` requests with upstream status `200` MUST be stored. Other statuses and error conditions MUST NOT be stored. |
| CACHE-2 | Two requests that the upstream would answer identically MUST share one entry. Requests that could receive different answers MUST NOT share an entry. |
| CACHE‑3 | An entry whose age exceeds the TTL MUST NOT be served. A request for it is a miss, and after a successful upstream response the entry MUST be stored again with a fresh TTL. |
| CACHE-4 | The number of entries MUST NOT exceed the configured capacity. |
| CACHE-5 | When a new entry is stored while the cache is at capacity, the least recently used entry MUST be evicted. |
| CACHE-6 | A hit MUST count as a use of the entry. Storing a value for an existing key MUST count as a use and MUST NOT evict any other entry. |
| CACHE-7 | Lookup, insertion and eviction MUST each run in O(1) time. |
 
## 7. Rate limiting
 
| ID | Requirement |
| --- | --- |
| RL‑1 | Each client MUST be limited to the configured number of requests per window. |
| RL-2 | A request over the limit MUST be rejected with status `429` and a `Retry-After` header. |
| RL-3 | A rejected request MUST NOT consult the cache and MUST NOT contact the upstream. |
| RL-4 | The limit MUST apply to all requests on `/proxy/**`, including requests that would have been hits. `/stats` MUST be exempt. |
| RL-5 | Limits MUST be tracked independently per client. A client reaching its limit MUST NOT affect other clients. |
| RL-6 | After the window has elapsed, a limited client MUST be able to send requests again. |
 
## 8. Upstream failures
 
| ID | Condition | Required behavior |
| --- | --- | --- |
| UF-1 | Upstream unreachable | Respond `502`. Nothing is cached. |
| UF‑2 | Upstream exceeds the configured timeout | Respond `504`. Nothing is cached. |
| UF-3 | Upstream returns a status other than `200` | Return that status to the client unchanged. Nothing is cached. |
| UF-4 | Any error response generated by the service | The body format MUST be consistent across all such responses. |
 
## 9. Concurrency
 
| ID | Requirement |
| --- | --- |
| CON‑1 | The service MUST handle concurrent requests correctly. |
| CON-2 | Under concurrent load, the cache MUST NOT exceed its capacity. |
| CON-3 | Under concurrent load, an expired entry MUST NOT be served. |
| CON-4 | Under concurrent load, statistics counters MUST NOT lose updates. |
| CON-5 | Under concurrent load, the rate limiter MUST NOT admit more requests than the limit within a window. |
| CON-6 | At least one automated test MUST exercise the cache from many threads and verify CON-2 and CON-3. |
 
## 10. Testing
 
| ID | Requirement |
| --- | --- |
| T-1 | Cache unit tests MUST cover LRU order (including that a read refreshes recency), update of an existing key, capacity, and TTL expiry. |
| T-2 | Rate limiter unit tests MUST cover under the limit, at the limit, over the limit, window reset, and independent clients. |
| T‑3 | End-to-end tests against a fake upstream MUST cover: miss followed by hit, refetch after expiry, non-`200` not cached, non-`GET` rejected, `429` behavior, and `502`/`504` mapping. |
| T-4 | The concurrency test required by CON-6 MUST be part of the automated suite. |
 
## 11. Deliverables
 
| ID | Deliverable |
| --- | --- |
| D-1 | A working service runnable with one documented command. |
| D‑2 | A `Dockerfile`. |
| D-3 | A README covering purpose, how to run, configuration reference, example requests showing a `MISS` followed by a `HIT`, and the rationale for each decision listed in section 15. |
| D-4 | This specification, stored in `docs/specs/`. |
| D-5 | Passing automated tests. |
 
## 12. Acceptance scenarios
 
Configuration for all scenarios: capacity 2, TTL 60 seconds, limit 5 requests per window.
 
| ID | Scenario | Expected outcome |
| --- | --- | --- |
| AS-1 | Request `A`, then `A` again | First response `MISS`, second `HIT`. The upstream received exactly one call. |
| AS-2 | Request `A`, `B`, `A`, then `C` | `B` is evicted. Requesting `A`, `C`, then `B` yields `HIT`, `HIT`, `MISS`. |
| AS‑3 | 60 seconds pass, then request `A` | Response `MISS`, and the entry is refreshed. |
| AS-4 | Upstream answers `404` for `X`, then `X` is requested twice | Both responses are `404`, and both requests reach the upstream. |
| AS-5 | One client sends 6 requests in one window | The sixth receives `429` with `Retry-After`. A different client is unaffected. |
| AS-6 | Request `/stats` after the scenarios above | The counters reflect all preceding requests. |
 
## 13. Out of scope
 
Authentication, persistence across restarts, distributed or multi-instance caching, caching of non-`GET` requests, and HTTPS configuration.
 
## 14. Optional requirements
 
The following are not required for acceptance. The service MAY additionally provide:
 
- Per-route TTL overrides.
- Serving a stale entry when the upstream is unavailable.
- Collapsing concurrent identical misses into a single upstream call.
- An endpoint to invalidate one entry or the whole cache.
- Honoring the upstream's `Cache-Control` headers.
## 15. Design decisions (non-normative)
 
This section is not part of the requirements. The specification deliberately leaves the following to the implementer. Each decision is to be made, implemented, and justified in the README (D-3).
 
- What makes up the cache key, and what is stored as the value.
- How the cache obtains the current time, and how that stays testable without real waiting.
- When expired entries are removed.
- How the cache is made safe under concurrency, and at what granularity.
- Which rate-limiting algorithm is used, and how a client is identified.
- Where in the request pipeline each component sits.
- Package and class structure.
 