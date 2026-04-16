package com.marcusprado02.commons.kernel.core

import java.util.UUID

public fun randomUuid(): UUID = UUID.randomUUID()

public fun String.toUuid(): UUID = UUID.fromString(this)

public fun String.toUuidOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
