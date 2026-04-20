package com.marcusprado02.commons.adapters.cache.redis

/** Strategy for serializing and deserializing cache values to/from byte arrays. */
public interface CacheSerializer {
    public fun <T : Any> serialize(value: T): ByteArray

    public fun <T : Any> deserialize(
        bytes: ByteArray,
        type: Class<T>,
    ): T
}
