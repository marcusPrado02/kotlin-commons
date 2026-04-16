package com.marcusprado02.commons.ports.persistence

public interface PageableRepository<E : Any, I : Any> : Repository<E, I> {
    public suspend fun findAll(request: PageRequest): PageResult<E>
    public suspend fun count(): Long
}
