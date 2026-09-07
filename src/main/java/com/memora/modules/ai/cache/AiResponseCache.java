package com.memora.modules.ai.cache;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, bounded in-memory cache for deterministic and repeated AI learning responses.
 * Avoids redundant external LLM calls and guarantees fast response times for identical queries.
 */
@Component
public class AiResponseCache {

    private static final int MAX_CACHE_SIZE = 1000;
    private static final long DEFAULT_TTL_MILLIS = 3600_000L; // 1 hour

    private record CacheEntry(Object data, long expiresAtMillis) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached value if present and unexpired.
     *
     * @param key Cache key
     * @param clazz Expected return class
     * @param <T> Return type
     * @return Cached instance, or null if missing/expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) return null;
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;

        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        if (clazz.isInstance(entry.data())) {
            return (T) entry.data();
        }
        return null;
    }

    /**
     * Stores a response in cache with default 1-hour TTL.
     */
    public void put(String key, Object value) {
        put(key, value, DEFAULT_TTL_MILLIS);
    }

    /**
     * Stores a response in cache with custom TTL.
     */
    public void put(String key, Object value, long ttlMillis) {
        if (key == null || value == null) return;

        // Evict expired entries if cache is growing large
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictExpired();
            if (cache.size() >= MAX_CACHE_SIZE) {
                // Clear roughly 20% oldest entries if still saturated
                cache.keySet().stream().limit(MAX_CACHE_SIZE / 5).forEach(cache::remove);
            }
        }

        long expiresAt = System.currentTimeMillis() + Math.max(1000L, ttlMillis);
        cache.put(key, new CacheEntry(value, expiresAt));
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    private void evictExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
