package com.marcusprado02.commons.adapters.messaging.kafka

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

internal data class TopicPartitionOffset(
    val topic: String,
    val partition: Int,
    val offset: Long,
) {
    fun toOffsetMap(): Map<TopicPartition, OffsetAndMetadata> =
        mapOf(TopicPartition(topic, partition) to OffsetAndMetadata(offset + 1))
}
