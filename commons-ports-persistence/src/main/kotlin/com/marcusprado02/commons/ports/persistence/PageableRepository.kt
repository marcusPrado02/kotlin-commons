package com.marcusprado02.commons.ports.persistence

/**
 * Extension of [Repository] that adds paginated retrieval and counting.
 *
 * @param E the entity type.
 * @param I the identifier type.
 */
public interface PageableRepository<E : Any, I : Any> : Repository<E, I> {
    /**
     * Returns a page of entities according to [request].
     *
     * @param request pagination and sorting parameters.
     */
    public suspend fun findAll(request: PageRequest): PageResult<E>

    /** Returns the total number of entities in the store. */
    public suspend fun count(): Long
}
