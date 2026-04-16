package com.marcusprado02.commons.ports.email

public interface EmailPort {
    public suspend fun send(email: Email)

    public suspend fun sendBatch(emails: List<Email>)
}
