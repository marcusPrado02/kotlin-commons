package com.marcusprado02.commons.adapters.cache.redis

import com.fasterxml.jackson.databind.ObjectMapper

/** [CacheSerializer] implementation backed by a Jackson [ObjectMapper]. */
public class JacksonCacheSerializer(
    private val objectMapper: ObjectMapper,
) : CacheSerializer {
    override fun <T : Any> serialize(value: T): ByteArray = objectMapper.writeValueAsBytes(value)

    override fun <T : Any> deserialize(
        bytes: ByteArray,
        type: Class<T>,
    ): T = objectMapper.readValue(bytes, type)
}
