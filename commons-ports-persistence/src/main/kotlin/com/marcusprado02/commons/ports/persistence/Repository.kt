package com.marcusprado02.commons.ports.persistence

/**
 * Base CRUD repository port for a single entity type.
 *
 * @param E the entity type.
 * @param I the identifier type.
 */
public interface Repository<E : Any, I : Any> {
    /**
     * Returns the entity with the given [id], or `null` if not found.
     *
     * @param id the entity identifier.
     */
    public suspend fun findById(id: I): E?

    /**
     * Persists [entity] and returns the saved state (which may differ from the input).
     *
     * @param entity the entity to save.
     */
    public suspend fun save(entity: E): E

    /**
     * Persists all [entities] and returns their saved states.
     * The default implementation delegates to [save] for each entity.
     *
     * @param entities the entities to save.
     */
    public suspend fun saveAll(entities: Collection<E>): List<E> = entities.map { save(it) }

    /**
     * Deletes [entity] from the store.
     *
     * @param entity the entity to delete.
     */
    public suspend fun delete(entity: E)

    /**
     * Deletes the entity with the given [id].
     *
     * @param id the identifier of the entity to delete.
     */
    public suspend fun deleteById(id: I)

    /**
     * Returns `true` if an entity with [id] exists in the store.
     *
     * @param id the entity identifier.
     */
    public suspend fun existsById(id: I): Boolean
}
