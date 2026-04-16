package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.audit.DeletionStamp
import com.marcusprado02.commons.kernel.ddd.entity.Entity
import com.marcusprado02.commons.kernel.ddd.entity.EntityVersion
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import com.marcusprado02.commons.kernel.ddd.identity.TenantId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import java.time.Instant

private val TENANT = TenantId("tenant-1")
private val ACTOR = ActorId("actor-1")
private val NOW = Instant.parse("2026-01-01T00:00:00Z")
private val STAMP = AuditStamp(ACTOR, NOW)
private val TRAIL = AuditTrail(STAMP, STAMP)

private class TestEntity(id: String, tenantId: TenantId, audit: AuditTrail) :
    Entity<String>(id, tenantId, EntityVersion.INITIAL, audit) {

    fun publicTouch(stamp: AuditStamp) = touch(stamp)
    fun publicSoftDelete(del: DeletionStamp, up: AuditStamp) = softDelete(del, up)
    fun publicRestore(stamp: AuditStamp) = restore(stamp)
}

class EntityTest : FunSpec({
    test("entity starts with initial version and not deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.id shouldBe "id-1"
        entity.tenantId shouldBe TENANT
        entity.version shouldBe EntityVersion.INITIAL
        entity.isDeleted shouldBe false
        entity.deletion.shouldBeNull()
    }

    test("touch increments version") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicTouch(STAMP)
        entity.version shouldBe EntityVersion(1L)
    }

    test("softDelete marks entity as deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        val delStamp = DeletionStamp(ACTOR, NOW)
        entity.publicSoftDelete(delStamp, STAMP)
        entity.isDeleted shouldBe true
        entity.deletion shouldBe delStamp
    }

    test("restore unmarks deleted entity") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        entity.publicRestore(STAMP)
        entity.isDeleted shouldBe false
        entity.deletion.shouldBeNull()
    }

    test("softDelete throws if already deleted") {
        val entity = TestEntity("id-1", TENANT, TRAIL)
        entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        shouldThrow<IllegalStateException> {
            entity.publicSoftDelete(DeletionStamp(ACTOR, NOW), STAMP)
        }
    }

    test("equals is based on id and tenantId") {
        val a = TestEntity("id-1", TENANT, TRAIL)
        val b = TestEntity("id-1", TENANT, TRAIL)
        val c = TestEntity("id-2", TENANT, TRAIL)
        (a == b) shouldBe true
        (a == c) shouldBe false
    }
})
