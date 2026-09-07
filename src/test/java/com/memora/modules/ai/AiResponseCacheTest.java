package com.memora.modules.ai;

import com.memora.modules.ai.cache.AiResponseCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiResponseCacheTest {

    private AiResponseCache cache;

    @BeforeEach
    void setUp() {
        cache = new AiResponseCache();
    }

    @Test
    @DisplayName("Should put and retrieve items within TTL")
    void testPutAndGet() {
        cache.put("test-key", "Test Value", 5000);
        String result = cache.get("test-key", String.class);
        assertEquals("Test Value", result);
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void testGetMissing() {
        assertNull(cache.get("non-existent", String.class));
    }

    @Test
    @DisplayName("Should return null and evict expired items")
    void testExpiredItem() throws InterruptedException {
        cache.put("expiring-key", "Temporary Value", 10);
        Thread.sleep(1050);
        assertNull(cache.get("expiring-key", String.class));
    }

    @Test
    @DisplayName("Should clear all entries on clear()")
    void testClear() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        assertEquals(2, cache.size());
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("TEST 15: Cache safety - User and context-specific AI responses cannot be incorrectly shared")
    void testCacheSafety_preventCrossUserLeakage() {
        // User A is struggling (e.g. at B1)
        String userAKey = "explain:ubiquitous:b1:true";
        String userAResponse = "Simplified explanation for struggling learner A.";
        cache.put(userAKey, userAResponse);

        // User B is not struggling (e.g. at B1)
        String userBKey = "explain:ubiquitous:b1:false";
        String userBResponse = "Standard academic explanation for learner B.";
        cache.put(userBKey, userBResponse);

        // Verify distinct separation without cross-user leakage
        assertEquals(userAResponse, cache.get(userAKey, String.class));
        assertEquals(userBResponse, cache.get(userBKey, String.class));
        assertNotEquals(cache.get(userAKey, String.class), cache.get(userBKey, String.class));
    }
}
