package com.marcusprado02.commons.kernel.ddd.query

/** Marker interface for all queries in the domain model. */
public interface Query<R>

/** Handles a [Q] query and produces a result of type [R]. */
public interface QueryHandler<Q : Query<R>, R> {
    public suspend fun handle(query: Q): R
}
