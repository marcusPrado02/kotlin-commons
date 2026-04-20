package com.marcusprado02.commons.kernel.errors

import kotlinx.serialization.Serializable

/** Indicates how severe a [Problem] is, from informational ([LOW]) to system-critical ([CRITICAL]). */
@Serializable
public enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
