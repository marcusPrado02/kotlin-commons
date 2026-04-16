package com.marcusprado02.commons.ports.persistence

public interface Repository<E : Any, I : Any> {
    public suspend fun findById(id: I): E?
    public suspend fun save(entity: E): E
    public suspend fun delete(entity: E)
    public suspend fun deleteById(id: I)
    public suspend fun existsById(id: I): Boolean
}
