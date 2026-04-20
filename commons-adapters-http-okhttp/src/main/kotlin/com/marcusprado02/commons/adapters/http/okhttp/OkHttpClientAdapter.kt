package com.marcusprado02.commons.adapters.http.okhttp

import com.marcusprado02.commons.ports.http.HttpClientPort
import com.marcusprado02.commons.ports.http.HttpRequest
import com.marcusprado02.commons.ports.http.HttpResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [HttpClientPort] implementation backed by OkHttp.
 *
 * Executes requests asynchronously and exposes them as suspending coroutine calls.
 * Per-request timeouts override the client-level call timeout when specified.
 *
 * @param client the pre-configured OkHttp client to use for requests.
 */
public class OkHttpClientAdapter(
    private val client: OkHttpClient,
) : HttpClientPort {
    override suspend fun execute(request: HttpRequest): HttpResponse<ByteArray> =
        suspendCancellableCoroutine { cont ->
            val timeout = request.timeout
            val effectiveClient =
                if (timeout != null) {
                    client
                        .newBuilder()
                        .callTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .build()
                } else {
                    client
                }
            val call = effectiveClient.newCall(request.toOkHttpRequest())
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) = cont.resumeWithException(e)

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) = cont.resume(response.toHttpResponse())
                },
            )
        }

    override suspend fun <T> execute(
        request: HttpRequest,
        mapper: (ByteArray) -> T,
    ): HttpResponse<T> = execute(request).map(mapper)
}
