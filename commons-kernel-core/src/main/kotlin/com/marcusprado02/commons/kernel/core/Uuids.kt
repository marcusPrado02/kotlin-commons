package com.marcusprado02.commons.kernel.core

import java.util.UUID

/** Generates a random [UUID]. */
public fun randomUuid(): UUID = UUID.randomUUID()

/**
 * Parses this string as a [UUID].
 *
 * @throws IllegalArgumentException if the string is not a valid UUID.
 */
public fun String.toUuid(): UUID = UUID.fromString(this)

/** Parses this string as a [UUID], or returns `null` if the string is not a valid UUID. */
public fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
