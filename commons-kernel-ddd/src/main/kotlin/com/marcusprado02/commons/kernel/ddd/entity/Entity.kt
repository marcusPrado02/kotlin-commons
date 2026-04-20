package com.marcusprado02.commons.kernel.ddd.entity

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.identity.TenantId

/**
 * Base class for all domain entities.
 *
 * Provides identity, tenant scoping, optimistic-concurrency versioning, audit trail,
 * and soft-delete support. Equality is based on [id] and [tenantId].
 *
 * @param I the type of the entity's identity.
 */
public abstract class Entity<I : Any>(
    public val id: I,
    public val tenantId: TenantId,
    initialVersion: EntityVersion = EntityVersion.INITIAL,
    initialAudit: AuditTrail,
    initialDeleted: Boolean = false,
    initialDeletion: DeletionStamp? = null,
) {
    public var version: EntityVersion = initialVersion
        private set
    public var audit: AuditTrail = initialAudit
        private set
    public var isDeleted: Boolean = initialDeleted
        private set
    public var deletion: DeletionStamp? = initialDeletion
        private set

    /**
     * Updates the audit trail and increments the version.
     *
     * @param updated the audit stamp for the change.
     */
    protected fun touch(updated: AuditStamp) {
        audit = audit.copy(updated = updated)
        version = version.increment()
    }

    /**
     * Marks the entity as soft-deleted.
     *
     * @param stamp deletion metadata.
     * @param updated the audit stamp for the operation.
     * @throws IllegalStateException if the entity is already deleted.
     */
    protected fun softDelete(
        stamp: DeletionStamp,
        updated: AuditStamp,
    ) {
        check(!isDeleted) { "Entity $id is already deleted" }
        isDeleted = true
        deletion = stamp
        touch(updated)
    }

    /**
     * Restores a previously soft-deleted entity.
     *
     * @param updated the audit stamp for the restoration.
     * @throws IllegalStateException if the entity is not deleted.
     */
    protected fun restore(updated: AuditStamp) {
        check(isDeleted) { "Entity $id is not deleted" }
        isDeleted = false
        deletion = null
        touch(updated)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false
        return id == other.id && tenantId == other.tenantId
    }

    override fun hashCode(): Int = 31 * id.hashCode() + tenantId.hashCode()

    override fun toString(): String = "${this::class.simpleName}(id=$id, tenantId=$tenantId)"
}
