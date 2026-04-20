package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.marcusprado02.commons.ports.cache.CacheKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands
import io.lettuce.core.codec.RedisCodec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import java.time.Duration

class RedisClusterCacheAdapterTest :
    FunSpec({
        val objectMapper = JsonMapper.builder().addModule(kotlinModule()).build()
        val serializer = JacksonCacheSerializer(objectMapper)

        val syncCommands = mockk<RedisAdvancedClusterCommands<String, ByteArray>>()
        val connection = mockk<StatefulRedisClusterConnection<String, ByteArray>>()
        val clusterClient = mockk<RedisClusterClient>()

        every { clusterClient.connect(any<RedisCodec<String, ByteArray>>()) } returns connection
        every { connection.sync() } returns syncCommands

        val adapter = RedisClusterCacheAdapter(clusterClient, serializer)

        test("get returns deserialized value on HIT") {
            runTest {
                val key = CacheKey("hit-key")
                val bytes = serializer.serialize("hello")
                every { syncCommands.get(key.value) } returns bytes

                val result = adapter.get(key, String::class.java)

                result shouldBe "hello"
            }
        }

        test("get returns null on MISS") {
            runTest {
                val key = CacheKey("miss-key")
                every { syncCommands.get(key.value) } returns null

                val result = adapter.get(key, String::class.java)

                result shouldBe null
            }
        }

        test("put with TTL calls setex") {
            runTest {
                val key = CacheKey("ttl-key")
                val ttl = Duration.ofSeconds(60)
                every { syncCommands.setex(key.value, ttl.toSeconds(), any<ByteArray>()) } returns "OK"

                adapter.put(key, "value", ttl)

                verify { syncCommands.setex(key.value, ttl.toSeconds(), any<ByteArray>()) }
            }
        }

        test("put without TTL calls set") {
            runTest {
                val key = CacheKey("no-ttl-key")
                every { syncCommands.set(key.value, any<ByteArray>()) } returns "OK"

                adapter.put(key, "value")

                verify { syncCommands.set(key.value, any<ByteArray>()) }
            }
        }

        test("remove calls del") {
            runTest {
                val key = CacheKey("del-key")
                every { syncCommands.del(key.value) } returns 1L

                adapter.remove(key)

                verify { syncCommands.del(key.value) }
            }
        }

        test("exists returns true when key count is positive") {
            runTest {
                val key = CacheKey("exists-key")
                every { syncCommands.exists(key.value) } returns 1L

                val result = adapter.exists(key)

                result shouldBe true
            }
        }

        test("exists returns false when key count is zero") {
            runTest {
                val key = CacheKey("absent-key")
                every { syncCommands.exists(key.value) } returns 0L

                val result = adapter.exists(key)

                result shouldBe false
            }
        }

        test("close closes the underlying connection") {
            runTest {
                every { connection.close() } returns Unit

                adapter.close()

                verify { connection.close() }
            }
        }
    })
