package com.tmv.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class Cache<K, V> {

    // Interner Cache-Speicher
    private final Map<K, CachedObject<V>> cache = new ConcurrentHashMap<>();

    /**
     * Insert object into cache or replaces it
     * @param key               key of object
     * @param value             value of object
     * @param durationInSeconds object validity in seconds
     */
    public void put(K key, V value, long durationInSeconds) {
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(durationInSeconds);
        cache.put(key, new CachedObject<>(value, expiryTime));
        log.info("Object with kes  {} put into cache; expiry in {}", key, expiryTime);
    }

    /**
     * check if object is sitll valid .
     *
     * @param key Key of object
     * @return true, if object is still valid, false otherwise
     */
    public boolean isValid(K key) {
        if (!cache.containsKey(key)) {
            return false;
        }
        CachedObject<V> cachedObject = cache.get(key);
        if (cachedObject.getExpiryTime().isBefore(LocalDateTime.now())) {
            cache.remove(key); // Entfernt abgelaufene Objekte.
            log.info("Object with key {} is expired and was removed.", key);
            return false;
        }
        return true;
    }

    /**
     * Retrieves object from cache, if valid
     *
     * @param key  key of object
     * @return cached object or null, if object is stale or not existing
     */
    public V get(K key) {
        if (isValid(key)) {
            return cache.get(key).getValue();
        }
        return null;
    }

    /**
     * Removes object from cache
     *
     * @param key key of object
     */
    public void remove(K key) {
        cache.remove(key);
        log.info("Object with key {} was removed from cache.", key);
    }

    /**
     * Clears cache e.
     */
    public void clear() {
        cache.clear();
        log.info("Cache was cleared.");
    }

    /**
     * Inner class to store cached object with validity time
     */
    @Data
    @AllArgsConstructor
    static class CachedObject<V> {
        private V value;
        private LocalDateTime expiryTime;
    }
}
