package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.marcusprado02.commons.ports.cache.CacheKey
import com.marcusprado02.commons.testkit.testcontainers.RedisContainers
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

class RedisCacheAdapterTest :
    FunSpec({
        val container = RedisContainers.instance
        val connectionFactory =
            LettuceConnectionFactory(
                RedisStandaloneConfiguration(container.host, container.getMappedPort(6379)),
            ).also { it.afterPropertiesSet() }
        val redis =
            RedisTemplate<String, ByteArray>().also {
                it.connectionFactory = connectionFactory
                it.keySerializer = StringRedisSerializer()
                it.valueSerializer = RedisSerializer.byteArray()
                it.afterPropertiesSet()
            }
        val objectMapper: ObjectMapper =
            JsonMapper
                .builder()
                .addModule(kotlinModule())
                .build()
        val adapter = RedisCacheAdapter(redis, objectMapper)

        beforeTest {
            redis.execute { it.serverCommands().flushDb() }
        }

        afterSpec {
            connectionFactory.destroy()
        }

        test("put and get round-trip with String") {
            runTest {
                adapter.put(CacheKey("k1"), "hello")
                val result = adapter.get(CacheKey("k1"), String::class.java)
                result shouldBe "hello"
            }
        }

        test("get returns null for missing key") {
            runTest {
                val result = adapter.get(CacheKey("missing"), String::class.java)
                result shouldBe null
            }
        }

        test("remove deletes the key") {
            runTest {
                adapter.put(CacheKey("k2"), "value")
                adapter.remove(CacheKey("k2"))
                adapter.get(CacheKey("k2"), String::class.java) shouldBe null
            }
        }

        test("exists returns true after put") {
            runTest {
                adapter.put(CacheKey("k3"), 42)
                adapter.exists(CacheKey("k3")) shouldBe true
            }
        }

        test("exists returns false for missing key") {
            runTest {
                adapter.exists(CacheKey("gone")) shouldBe false
            }
        }

        test("clear removes all keys") {
            runTest {
                adapter.put(CacheKey("a"), "x")
                adapter.put(CacheKey("b"), "y")
                adapter.clear()
                adapter.get(CacheKey("a"), String::class.java) shouldBe null
                adapter.get(CacheKey("b"), String::class.java) shouldBe null
            }
        }

        test("put with TTL stores value that can be retrieved") {
            runTest {
                adapter.put(CacheKey("ttl-key"), "temporary", ttl = Duration.ofSeconds(30))
                adapter.get(CacheKey("ttl-key"), String::class.java) shouldNotBe null
            }
        }
    })
