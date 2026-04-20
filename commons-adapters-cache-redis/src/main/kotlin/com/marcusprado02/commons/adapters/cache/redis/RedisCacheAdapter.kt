package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.ports.cache.CachePort
import com.marcusprado02.commons.ports.persistence.PersistenceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

public class RedisCacheAdapter(
    private val redis: RedisTemplate<String, ByteArray>,
    private val serializer: CacheSerializer = defaultSerializer(),
    public val poolConfig: RedisPoolConfig = RedisPoolConfig(),
) : CachePort {
    public constructor(
        redis: RedisTemplate<String, ByteArray>,
        objectMapper: ObjectMapper,
    ) : this(redis, JacksonCacheSerializer(objectMapper))

    public constructor(
        redis: RedisTemplate<String, ByteArray>,
        objectMapper: ObjectMapper,
        poolConfig: RedisPoolConfig,
    ) : this(redis, JacksonCacheSerializer(objectMapper), poolConfig)

    override suspend fun <T : Any> get(
        key: CacheKey,
        type: Class<T>,
    ): T? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = redis.opsForValue().get(key.value)
                val value = bytes?.let { serializer.deserialize(it, type) }
                if (value != null) {
                    log.debug("Cache HIT for key={}", key.value)
                } else {
                    log.debug("Cache MISS for key={}", key.value)
                }
                value
            } catch (ex: RedisConnectionFailureException) {
                throw PersistenceException("Redis get failed for key '${key.value}'", ex)
            }
        }

    override suspend fun <T : Any> put(
        key: CacheKey,
        value: T,
        ttl: Duration?,
    ): Unit =
        withContext(Dispatchers.IO) {
            try {
                val bytes = serializer.serialize(value)
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
                redis.execute<Unit> { it.serverCommands().flushDb() }
            } catch (ex: DataAccessException) {
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

    private companion object {
        private val log = LoggerFactory.getLogger(RedisCacheAdapter::class.java)

        private fun defaultSerializer(): CacheSerializer =
            JacksonCacheSerializer(
                JsonMapper.builder().addModule(kotlinModule()).build(),
            )
    }
}
