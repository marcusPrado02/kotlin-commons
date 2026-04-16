package com.marcusprado02.commons.ports.http

public sealed class HttpBody {
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

    public data class FormUrlEncoded(
        val params: Map<String, String>,
    ) : HttpBody()

    public data class Multipart(
        val parts: List<MultipartPart>,
    ) : HttpBody()
}

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
