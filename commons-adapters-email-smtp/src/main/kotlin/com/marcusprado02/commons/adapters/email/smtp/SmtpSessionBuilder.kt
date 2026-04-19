package com.marcusprado02.commons.adapters.email.smtp

import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import java.util.Properties

public class SmtpSessionBuilder {
    private var host: String = "localhost"
    private var port: Int = DEFAULT_PORT
    private var username: String? = null
    private var password: String? = null
    private var useTls: Boolean = true
    private var connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS
    private var readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS

    public fun host(host: String): SmtpSessionBuilder = apply { this.host = host }

    public fun port(port: Int): SmtpSessionBuilder = apply { this.port = port }

    public fun credentials(
        username: String,
        password: String,
    ): SmtpSessionBuilder =
        apply {
            this.username = username
            this.password = password
        }

    public fun tls(enabled: Boolean): SmtpSessionBuilder = apply { this.useTls = enabled }

    public fun connectTimeout(ms: Int): SmtpSessionBuilder = apply { this.connectTimeoutMs = ms }

    public fun readTimeout(ms: Int): SmtpSessionBuilder = apply { this.readTimeoutMs = ms }

    public fun build(): Session {
        val props =
            Properties().apply {
                put("mail.smtp.host", host)
                put("mail.smtp.port", port.toString())
                put("mail.smtp.starttls.enable", useTls.toString())
                put("mail.smtp.connectiontimeout", connectTimeoutMs.toString())
                put("mail.smtp.timeout", readTimeoutMs.toString())
                if (username != null) {
                    put("mail.smtp.auth", "true")
                }
            }
        val auth =
            username?.let { user ->
                object : Authenticator() {
                    override fun getPasswordAuthentication() = PasswordAuthentication(user, password ?: "")
                }
            }
        return if (auth != null) Session.getInstance(props, auth) else Session.getInstance(props)
    }

    private companion object {
        private const val DEFAULT_PORT = 587
        private const val DEFAULT_CONNECT_TIMEOUT_MS = 5_000
        private const val DEFAULT_READ_TIMEOUT_MS = 10_000
    }
}
