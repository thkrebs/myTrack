package com.tmv.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new Cache<>();
    }

    @Test
    void put_shouldAddObjectToCache() {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cache.put(key, value, 60); // 60 Sekunden Validität

        // Assert
        assertTrue(cache.isValid(key));
        assertEquals(value, cache.get(key));
    }

    @Test
    void get_shouldReturnNull_whenObjectIsExpired() throws InterruptedException {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cache.put(key, value, 1); // 1 Sekunde Validität
        Thread.sleep(2000); // Warten, bis das Objekt abläuft

        // Assert
        assertFalse(cache.isValid(key));
        assertNull(cache.get(key));
    }

    @Test
    void isValid_shouldReturnTrue_whenObjectIsStillValid() {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cache.put(key, value, 60);

        // Assert
        assertTrue(cache.isValid(key));
    }

    @Test
    void isValid_shouldReturnFalse_whenObjectDoesNotExist() {
        // Arrange
        String key = "nonExistentKey";

        // Act & Assert
        assertFalse(cache.isValid(key));
    }

    @Test
    void isValid_shouldReturnFalse_whenObjectIsExpired() throws InterruptedException {
        // Arrange
        String key = "testKey";
        String value = "testValue";

        // Act
        cache.put(key, value, 1); // 1 Sekunde Validität
        Thread.sleep(2000); // Warten, bis das Objekt abläuft

        // Assert
        assertFalse(cache.isValid(key));
    }

    @Test
    void remove_shouldDeleteObjectFromCache() {
        // Arrange
        String key = "testKey";
        String value = "testValue";
        cache.put(key, value, 60);

        // Act
        cache.remove(key);

        // Assert
        assertFalse(cache.isValid(key));
        assertNull(cache.get(key));
    }

    @Test
    void clear_shouldEmptyTheCache() {
        // Arrange
        cache.put("key1", "value1", 60);
        cache.put("key2", "value2", 60);

        // Act
        cache.clear();

        // Assert
        assertFalse(cache.isValid("key1"));
        assertFalse(cache.isValid("key2"));
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    @Test
    void put_shouldReplaceExistingObjectInCache() {
        // Arrange
        String key = "testKey";
        String value1 = "value1";
        String value2 = "value2";
        cache.put(key, value1, 60);

        // Act
        cache.put(key, value2, 60);

        // Assert
        assertTrue(cache.isValid(key));
        assertEquals(value2, cache.get(key));
    }

    @Test
    void get_shouldReturnNull_whenKeyDoesNotExist() {
        // Arrange
        String key = "missingKey";

        // Act
        String result = cache.get(key);

        // Assert
        assertNull(result);
    }
}