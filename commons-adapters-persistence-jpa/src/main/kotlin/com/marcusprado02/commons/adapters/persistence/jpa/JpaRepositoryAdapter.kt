package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PersistenceException
import com.marcusprado02.commons.ports.persistence.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DataAccessException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException

/**
 * Abstract [Repository] adapter that delegates to a Spring Data [JpaRepository].
 *
 * All database calls are dispatched on [kotlinx.coroutines.Dispatchers.IO] and wrapped in
 * [PersistenceException] to decouple callers from Spring Data exceptions. Optimistic locking
 * failures are explicitly mapped to [PersistenceException] with a descriptive message.
 *
 * @param E the entity type.
 * @param I the entity's identity type.
 */
public abstract class JpaRepositoryAdapter<E : Any, I : Any>(
    protected val jpa: JpaRepository<E, I>,
) : Repository<E, I> {
    override suspend fun findById(id: I): E? =
        withContext(Dispatchers.IO) {
            try {
                jpa.findById(id).orElse(null)
            } catch (ex: DataAccessException) {
                throw PersistenceException("findById failed", ex)
            }
        }

    override suspend fun save(entity: E): E =
        withContext(Dispatchers.IO) {
            try {
                jpa.save(entity)
            } catch (ex: ObjectOptimisticLockingFailureException) {
                throw PersistenceException("Optimistic locking failure: entity was modified concurrently", ex)
            } catch (ex: DataAccessException) {
                throw PersistenceException("save failed", ex)
            }
        }

    override suspend fun delete(entity: E): Unit =
        withContext(Dispatchers.IO) {
            try {
                jpa.delete(entity)
            } catch (ex: DataAccessException) {
                throw PersistenceException("delete failed", ex)
            }
        }

    override suspend fun deleteById(id: I): Unit =
        withContext(Dispatchers.IO) {
            try {
                jpa.deleteById(id)
            } catch (ex: DataAccessException) {
                throw PersistenceException("deleteById failed", ex)
            }
        }

    override suspend fun existsById(id: I): Boolean =
        withContext(Dispatchers.IO) {
            try {
                jpa.existsById(id)
            } catch (ex: DataAccessException) {
                throw PersistenceException("existsById failed", ex)
            }
        }

    override suspend fun saveAll(entities: Collection<E>): List<E> =
        withContext(Dispatchers.IO) {
            try {
                jpa.saveAll(entities)
            } catch (ex: ObjectOptimisticLockingFailureException) {
                throw PersistenceException("Optimistic locking failure: entity was modified concurrently", ex)
            } catch (ex: DataAccessException) {
                throw PersistenceException("saveAll failed", ex)
            }
        }
}
