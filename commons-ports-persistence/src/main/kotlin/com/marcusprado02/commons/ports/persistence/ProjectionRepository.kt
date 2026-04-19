package com.marcusprado02.commons.ports.persistence

/** Read-only repository that returns projections [P] instead of full entities. */
public interface ProjectionRepository<I : Any, P : Any> {
    public suspend fun findById(id: I): P?

    public suspend fun findAll(request: PageRequest): PageResult<P>
}
