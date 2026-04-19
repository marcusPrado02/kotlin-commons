package com.marcusprado02.commons.kernel.core

public fun <T> Collection<T>.secondOrNull(): T? = if (size >= 2) elementAt(1) else null

public fun <T> Collection<T>.second(): T = secondOrNull() ?: throw NoSuchElementException("Collection has less than 2 elements")

public fun <T> List<T>.updated(
    index: Int,
    element: T,
): List<T> = toMutableList().also { it[index] = element }

public fun <K, V> Map<K, V>.mergeWith(
    other: Map<K, V>,
    mergeValues: (V, V) -> V,
): Map<K, V> {
    val result = toMutableMap()
    other.forEach { (k, v) -> result[k] = result[k]?.let { mergeValues(it, v) } ?: v }
    return result
}

public fun <T> List<T>.splitWhen(predicate: (T) -> Boolean): List<List<T>> {
    if (isEmpty()) return listOf(emptyList())
    val result = mutableListOf<MutableList<T>>()
    var current = mutableListOf<T>()
    for (element in this) {
        if (predicate(element) && current.isNotEmpty()) {
            result.add(current)
            current = mutableListOf()
        }
        current.add(element)
    }
    result.add(current)
    return result
}
