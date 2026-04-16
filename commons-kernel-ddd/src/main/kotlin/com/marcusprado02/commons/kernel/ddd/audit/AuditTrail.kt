package com.marcusprado02.commons.kernel.ddd.audit

public data class AuditTrail(
    val created: AuditStamp,
    val updated: AuditStamp,
)
