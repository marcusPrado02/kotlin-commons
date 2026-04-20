package com.marcusprado02.commons.ports.email

/**
 * A validated email address, optionally paired with a display name.
 *
 * @property address the raw email address (must contain `@`).
 * @property displayName optional human-readable name shown alongside the address.
 */
public data class EmailAddress(
    val address: String,
    val displayName: String? = null,
) {
    init {
        require(address.contains("@")) { "Invalid email address: $address" }
    }

    override fun toString(): String = if (displayName != null) "$displayName <$address>" else address
}
