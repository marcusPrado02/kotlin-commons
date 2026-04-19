package com.marcusprado02.commons.ports.cache

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration

private class InMemoryCache : CachePort {
    private val store = mutableMapOf<String, Any>()

    override suspend fun <T : Any> get(
        key: CacheKey,
        type: Class<T>,
    ): T? {
        @Suppress("UNCHECKED_CAST")
        return store[key.value] as? T
    }

    override suspend fun <T : Any> put(
        key: CacheKey,
        value: T,
        ttl: Duration?,
    ) {
        store[key.value] = value
    }

    override suspend fun remove(key: CacheKey) {
        store.remove(key.value)
    }

    override suspend fun clear() {
        store.clear()
    }

    override suspend fun exists(key: CacheKey): Boolean = store.containsKey(key.value)
}

class CachePortTest :
    FunSpec({
        test("put and get round-trip") {
            val cache = InMemoryCache()
            cache.put(CacheKey("k1"), "value1")
            cache.get<String>(CacheKey("k1")) shouldBe "value1"
        }

        test("remove deletes entry") {
            val cache = InMemoryCache()
            cache.put(CacheKey("k1"), "v1")
            cache.remove(CacheKey("k1"))
            cache.get<String>(CacheKey("k1")).shouldBeNull()
        }

        test("exists returns false for missing key") {
            val cache = InMemoryCache()
            cache.exists(CacheKey("missing")) shouldBe false
        }

        test("CacheKey rejects blank value") {
            val ex = runCatching { CacheKey("") }.exceptionOrNull()
            (ex is IllegalArgumentException) shouldBe true
        }

        test("getOrPut loads value when cache miss") {
            kotlinx.coroutines.test.runTest {
                val cache = InMemoryCache()
                var loaderCalled = 0
                val result =
                    cache.getOrPut(CacheKey("k")) {
                        loaderCalled++
                        "loaded-value"
                    }
                result shouldBe "loaded-value"
                loaderCalled shouldBe 1
            }
        }

        test("getOrPut returns cached value without calling loader") {
            kotlinx.coroutines.test.runTest {
                val cache = InMemoryCache()
                cache.put(CacheKey("k"), "cached")
                var loaderCalled = 0
                val result =
                    cache.getOrPut(CacheKey("k")) {
                        loaderCalled++
                        "new-value"
                    }
                result shouldBe "cached"
                loaderCalled shouldBe 0
            }
        }

        test("getAll returns only present keys") {
            kotlinx.coroutines.test.runTest {
                val cache = InMemoryCache()
                cache.put(CacheKey("a"), "val-a")
                cache.put(CacheKey("c"), "val-c")
                val keys = setOf(CacheKey("a"), CacheKey("b"), CacheKey("c"))
                val result = cache.getAll<String>(keys)
                result.size shouldBe 2
                result[CacheKey("a")] shouldBe "val-a"
                result[CacheKey("c")] shouldBe "val-c"
            }
        }

        test("getAll with all missing keys returns empty map") {
            kotlinx.coroutines.test.runTest {
                val cache = InMemoryCache()
                val result = cache.getAll<String>(setOf(CacheKey("x"), CacheKey("y")))
                result shouldBe emptyMap()
            }
        }

        test("invalidateByPrefix is no-op by default") {
            kotlinx.coroutines.test.runTest {
                val cache = InMemoryCache()
                cache.put(CacheKey("prefix:key1"), "v1")
                cache.invalidateByPrefix("prefix:")
                // Default implementation is no-op; entry still exists
                cache.get<String>(CacheKey("prefix:key1")) shouldBe "v1"
            }
        }
    })
