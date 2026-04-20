package com.marcusprado02.commons.ports.http

/** Sealed hierarchy representing the body of an HTTP request. */
public sealed class HttpBody {
    /**
     * Raw byte body with an explicit MIME type.
     *
     * @property content the raw bytes.
     * @property contentType MIME type of the content (e.g. `"application/octet-stream"`).
     */
    public class Bytes(
        public val content: ByteArray,
        public val contentType: String,
    ) : HttpBody() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false
            return content.contentEquals(other.content) && contentType == other.contentType
        }

        override fun hashCode(): Int = 31 * content.contentHashCode() + contentType.hashCode()

        override fun toString(): String = "Bytes(contentType='$contentType', size=${content.size})"
    }

    /** URL-encoded form body (`application/x-www-form-urlencoded`). */
    public data class FormUrlEncoded(
        val params: Map<String, String>,
    ) : HttpBody()

    /** Multipart form body composed of one or more [MultipartPart]s. */
    public data class Multipart(
        val parts: List<MultipartPart>,
    ) : HttpBody()

    /**
     * JSON body serialized using kotlinx.serialization.
     *
     * @property value the object to serialize.
     * @property serializer the serializer for [value].
     * @property format the JSON format to use; defaults to [kotlinx.serialization.json.Json].
     */
    public class Json<T>(
        public val value: T,
        public val serializer: kotlinx.serialization.KSerializer<T>,
        public val format: kotlinx.serialization.json.Json = kotlinx.serialization.json.Json,
    ) : HttpBody() {
        /** Serializes [value] to a UTF-8 encoded JSON byte array. */
        public fun toBytes(): ByteArray = format.encodeToString(serializer, value).toByteArray(Charsets.UTF_8)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Json<*>) return false
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = "Json(value=$value)"
    }
}

/**
 * A single part in a multipart HTTP body.
 *
 * @property name form field name.
 * @property content raw bytes of this part.
 * @property contentType MIME type of this part.
 * @property filename optional filename hint for file-upload parts.
 */
public class MultipartPart(
    public val name: String,
    public val content: ByteArray,
    public val contentType: String,
    public val filename: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartPart) return false
        return name == other.name &&
            content.contentEquals(other.content) &&
            contentType == other.contentType &&
            filename == other.filename
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + (filename?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "MultipartPart(name='$name', contentType='$contentType', filename=$filename, size=${content.size})"
}
