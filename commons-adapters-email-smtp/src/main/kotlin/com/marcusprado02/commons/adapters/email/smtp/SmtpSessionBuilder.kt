package com.marcusprado02.commons.adapters.email.smtp

import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import java.util.Properties

/**
 * Fluent builder for creating a Jakarta Mail SMTP [jakarta.mail.Session].
 *
 * Defaults: host `localhost`, port `587`, TLS enabled, connect timeout 5 s, read timeout 10 s.
 */
public class SmtpSessionBuilder {
    private var host: String = "localhost"
    private var port: Int = DEFAULT_PORT
    private var username: String? = null
    private var password: String? = null
    private var useTls: Boolean = true
    private var connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MS
    private var readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MS

    /** Sets the SMTP server hostname. */
    public fun host(host: String): SmtpSessionBuilder = apply { this.host = host }

    /** Sets the SMTP server port. */
    public fun port(port: Int): SmtpSessionBuilder = apply { this.port = port }

    /**
     * Sets SMTP authentication credentials.
     *
     * @param username the SMTP username.
     * @param password the SMTP password.
     */
    public fun credentials(
        username: String,
        password: String,
    ): SmtpSessionBuilder =
        apply {
            this.username = username
            this.password = password
        }

    /** Enables or disables STARTTLS. */
    public fun tls(enabled: Boolean): SmtpSessionBuilder = apply { this.useTls = enabled }

    /** Sets the connection establishment timeout in milliseconds. */
    public fun connectTimeout(ms: Int): SmtpSessionBuilder = apply { this.connectTimeoutMs = ms }

    /** Sets the socket read timeout in milliseconds. */
    public fun readTimeout(ms: Int): SmtpSessionBuilder = apply { this.readTimeoutMs = ms }

    /**
     * Builds and returns a configured [jakarta.mail.Session].
     *
     * @return the configured Jakarta Mail session.
     */
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
