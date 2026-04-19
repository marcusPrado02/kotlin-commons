package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.ports.cache.CachePort
import com.marcusprado02.commons.ports.persistence.PersistenceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

public class RedisCacheAdapter(
    private val redis: RedisTemplate<String, ByteArray>,
    private val objectMapper: ObjectMapper,
) : CachePort {

    override suspend fun <T : Any> get(key: CacheKey, type: Class<T>): T? =
        withContext(Dispatchers.IO) {
            try {
                redis.opsForValue().get(key.value)?.let { objectMapper.readValue(it, type) }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis get failed for key '${key.value}'", ex)
            }
        }

    override suspend fun <T : Any> put(key: CacheKey, value: T, ttl: Duration?): Unit =
        withContext(Dispatchers.IO) {
            try {
                val bytes = objectMapper.writeValueAsBytes(value)
                if (ttl != null) {
                    redis.opsForValue().set(key.value, bytes, ttl)
                } else {
                    redis.opsForValue().set(key.value, bytes)
                }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis put failed for key '${key.value}'", ex)
            }
        }

    override suspend fun remove(key: CacheKey): Unit =
        withContext(Dispatchers.IO) {
            try {
                redis.delete(key.value)
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis remove failed for key '${key.value}'", ex)
            }
        }

    override suspend fun clear(): Unit =
        withContext(Dispatchers.IO) {
            try {
                redis.execute<Void?> { it.serverCommands().flushDb(); null }
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis clear failed", ex)
            }
        }

    override suspend fun exists(key: CacheKey): Boolean =
        withContext(Dispatchers.IO) {
            try {
                redis.hasKey(key.value) == true
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis exists failed for key '${key.value}'", ex)
            }
        }
}
