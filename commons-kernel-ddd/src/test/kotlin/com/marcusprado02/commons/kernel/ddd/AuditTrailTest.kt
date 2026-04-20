package com.marcusprado02.commons.kernel.ddd

import com.marcusprado02.commons.kernel.ddd.audit.AuditStamp
import com.marcusprado02.commons.kernel.ddd.audit.AuditTrail
import com.marcusprado02.commons.kernel.ddd.identity.ActorId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuditTrailTest :
    FunSpec({
        val actor = ActorId("system")
        val now = Instant.parse("2026-04-20T12:00:00Z")
        val stamp = AuditStamp(actor, now)
        val trail = AuditTrail(created = stamp, updated = stamp)

        test("deleted is null by default") {
            trail.deleted.shouldBeNull()
        }

        test("markDeleted fills in DeletionStamp with actor and clock instant") {
            val deleteActor = ActorId("admin")
            val deleteTime = Instant.parse("2026-04-21T08:00:00Z")
            val clock = Clock.fixed(deleteTime, ZoneOffset.UTC)

            val deleted = trail.markDeleted(deleteActor, clock)

            deleted.deleted.shouldNotBeNull()
            deleted.deleted!!.actorId shouldBe deleteActor
            deleted.deleted!!.at shouldBe deleteTime
            deleted.deleted!!.reason.shouldBeNull()
        }

        test("markDeleted preserves created and updated stamps") {
            val clock = Clock.fixed(now, ZoneOffset.UTC)
            val deleted = trail.markDeleted(actor, clock)

            deleted.created shouldBe stamp
            deleted.updated shouldBe stamp
        }

        test("markDeleted with reason stores the reason") {
            val clock = Clock.fixed(now, ZoneOffset.UTC)
            val deleted = trail.markDeleted(actor, clock, reason = "GDPR erasure request")

            deleted.deleted!!.reason shouldBe "GDPR erasure request"
        }

        test("original trail is unchanged after markDeleted") {
            val clock = Clock.fixed(now, ZoneOffset.UTC)
            trail.markDeleted(actor, clock)

            trail.deleted.shouldBeNull()
        }
    })
