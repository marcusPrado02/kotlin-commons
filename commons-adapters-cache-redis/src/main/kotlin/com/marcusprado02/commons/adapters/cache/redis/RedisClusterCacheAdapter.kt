package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.ports.cache.CachePort
import com.marcusprado02.commons.ports.persistence.PersistenceException
import io.lettuce.core.RedisCommandExecutionException
import io.lettuce.core.RedisConnectionException
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration

/**
 * [CachePort] implementation backed by a Redis Cluster via Lettuce.
 *
 * Serializes values to byte arrays using the supplied [CacheSerializer] and executes all
 * Redis I/O on [kotlinx.coroutines.Dispatchers.IO]. Implements [Closeable] to allow explicit
 * release of the underlying cluster connection.
 *
 * Note: [clear] flushes only the local node's database. For a full cluster-wide flush,
 * iterate all cluster nodes individually; this operation is intended for non-production use.
 */
public class RedisClusterCacheAdapter(
    clusterClient: RedisClusterClient,
    private val serializer: CacheSerializer =
        JacksonCacheSerializer(JsonMapper.builder().addModule(kotlinModule()).build()),
) : CachePort,
    Closeable {
    private val connection: StatefulRedisClusterConnection<String, ByteArray> =
        clusterClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

    private fun sync(): RedisAdvancedClusterCommands<String, ByteArray> = connection.sync()

    override suspend fun <T : Any> get(
        key: CacheKey,
        type: Class<T>,
    ): T? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = sync().get(key.value)
                val value = bytes?.let { serializer.deserialize(it, type) }
                if (value != null) {
                    log.debug("Cache HIT for key={}", key.value)
                } else {
                    log.debug("Cache MISS for key={}", key.value)
                }
                value
            } catch (ex: RedisCommandExecutionException) {
                throw PersistenceException("Redis cluster get failed for key '${key.value}'", ex)
            } catch (ex: RedisConnectionException) {
                throw PersistenceException("Redis cluster get failed for key '${key.value}'", ex)
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
                    sync().setex(key.value, ttl.toSeconds(), bytes)
                } else {
                    sync().set(key.value, bytes)
                }
            } catch (ex: RedisCommandExecutionException) {
                throw PersistenceException("Redis cluster put failed for key '${key.value}'", ex)
            } catch (ex: RedisConnectionException) {
                throw PersistenceException("Redis cluster put failed for key '${key.value}'", ex)
            }
        }

    override suspend fun remove(key: CacheKey): Unit =
        withContext(Dispatchers.IO) {
            try {
                sync().del(key.value)
            } catch (ex: RedisCommandExecutionException) {
                throw PersistenceException("Redis cluster remove failed for key '${key.value}'", ex)
            } catch (ex: RedisConnectionException) {
                throw PersistenceException("Redis cluster remove failed for key '${key.value}'", ex)
            }
        }

    override suspend fun clear(): Unit =
        withContext(Dispatchers.IO) {
            try {
                // NOTE: In a Redis Cluster, flushdb() only flushes the local node's database.
                // For a full cluster-wide flush, iterate over all cluster nodes and flush each one.
                // This implementation is intended for non-production/testing use only.
                sync().flushdb()
            } catch (ex: RedisCommandExecutionException) {
                throw PersistenceException("Redis cluster clear failed", ex)
            } catch (ex: RedisConnectionException) {
                throw PersistenceException("Redis cluster clear failed", ex)
            }
        }

    override suspend fun exists(key: CacheKey): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sync().exists(key.value) > 0L
            } catch (ex: RedisCommandExecutionException) {
                throw PersistenceException("Redis cluster exists failed for key '${key.value}'", ex)
            } catch (ex: RedisConnectionException) {
                throw PersistenceException("Redis cluster exists failed for key '${key.value}'", ex)
            }
        }

    override fun close() {
        connection.close()
    }

    private companion object {
        private val log = LoggerFactory.getLogger(RedisClusterCacheAdapter::class.java)
    }
}
