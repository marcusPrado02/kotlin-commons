package com.marcusprado02.commons.ports.persistence

public open class PersistenceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public class EntityNotFoundException(
    message: String,
) : PersistenceException(message)

public class OptimisticLockException(
    message: String,
    cause: Throwable? = null,
) : PersistenceException(message, cause)
