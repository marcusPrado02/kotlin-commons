package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpBody
import com.marcusprado02.commons.ports.http.HttpRequest
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

internal fun HttpRequest.toOkHttpRequest(): Request {
    val builder = Request.Builder().url(uri.toString())
    headers.forEach { (name, value) -> builder.addHeader(name, value) }
    builder.method(method.name, body?.toRequestBody())
    return builder.build()
}

private fun HttpBody.toRequestBody(): RequestBody? =
    when (this) {
        is HttpBody.Bytes -> content.toRequestBody(contentType.toMediaType())
        is HttpBody.FormUrlEncoded ->
            FormBody
                .Builder()
                .also { fb -> params.forEach { (k, v) -> fb.add(k, v) } }
                .build()
        is HttpBody.Multipart ->
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .also { mb ->
                    parts.forEach { part ->
                        if (part.filename != null) {
                            mb.addFormDataPart(
                                part.name,
                                part.filename,
                                part.content.toRequestBody(part.contentType.toMediaType()),
                            )
                        } else {
                            mb.addFormDataPart(part.name, String(part.content))
                        }
                    }
                }.build()
        is HttpBody.Json<*> -> this.toBytes().toRequestBody("application/json; charset=utf-8".toMediaType())
    }
