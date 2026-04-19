package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.ports.persistence.PageResult
import com.marcusprado02.commons.ports.persistence.PageableRepository
import com.marcusprado02.commons.ports.persistence.PersistenceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DataAccessException
import org.springframework.data.jpa.repository.JpaRepository

public abstract class JpaPageableRepositoryAdapter<E : Any, I : Any>(
    private val jpa: JpaRepository<E, I>,
) : JpaRepositoryAdapter<E, I>(jpa),
    PageableRepository<E, I> {
    override suspend fun findAll(request: PageRequest): PageResult<E> =
        withContext(Dispatchers.IO) {
            try {
                jpa.findAll(request.toSpringPageable()).toPageResult()
            } catch (ex: DataAccessException) {
                throw PersistenceException("findAll failed", ex)
            }
        }

    override suspend fun count(): Long =
        withContext(Dispatchers.IO) {
            try {
                jpa.count()
            } catch (ex: DataAccessException) {
                throw PersistenceException("count failed", ex)
            }
        }
}
