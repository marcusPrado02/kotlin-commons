package com.marcusprado02.commons.ports.email

public data class EmailAddress(
    val address: String,
    val displayName: String? = null,
) {
    init {
        require(address.contains("@")) { "Invalid email address: $address" }
    }

    override fun toString(): String = if (displayName != null) "$displayName <$address>" else address
}
