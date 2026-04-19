package com.marcusprado02.commons.adapters.persistence.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TestJpaRepository : JpaRepository<TestEntity, UUID>
