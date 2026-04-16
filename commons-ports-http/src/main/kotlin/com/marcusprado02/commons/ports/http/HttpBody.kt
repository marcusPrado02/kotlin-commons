package com.marcusprado02.commons.ports.http

public sealed class HttpBody {
    public data class Bytes(
        val content: ByteArray,
        val contentType: String,
    ) : HttpBody()

    public data class FormUrlEncoded(
        val params: Map<String, String>,
    ) : HttpBody()

    public data class Multipart(
        val parts: List<MultipartPart>,
    ) : HttpBody()
}

public data class MultipartPart(
    val name: String,
    val content: ByteArray,
    val contentType: String,
    val filename: String? = null,
)
