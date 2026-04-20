package com.marcusprado02.commons.kernel.ddd.audit

/**
 * Tracks the creation and last-update audit stamps for an entity.
 *
 * @property created stamp recorded when the entity was first persisted.
 * @property updated stamp recorded on the most recent mutation.
 */
public data class AuditTrail(
    val created: AuditStamp,
    val updated: AuditStamp,
)
