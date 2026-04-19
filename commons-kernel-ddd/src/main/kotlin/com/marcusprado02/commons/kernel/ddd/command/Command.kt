package com.marcusprado02.commons.kernel.ddd.command

/** Marker interface for all commands in the domain model. */
public interface Command

/** Handles a [C] command and produces a result of type [R]. */
public interface CommandHandler<C : Command, R> {
    public suspend fun handle(command: C): R
}
