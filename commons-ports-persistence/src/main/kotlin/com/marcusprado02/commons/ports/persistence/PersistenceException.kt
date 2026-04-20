package com.marcusprado02.commons.ports.persistence

/** Base class for persistence-layer failures. */
public open class PersistenceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/** Thrown when a requested entity cannot be found in the persistence store. */
public class EntityNotFoundException(
    message: String,
) : PersistenceException(message)

/** Thrown when a concurrent modification conflict is detected (optimistic locking failure). */
public class OptimisticLockException(
    message: String,
    cause: Throwable? = null,
) : PersistenceException(message, cause)
