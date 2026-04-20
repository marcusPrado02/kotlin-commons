package com.marcusprado02.commons.kernel.core

/** Returns the second element, or `null` if the collection has fewer than 2 elements. */
public fun <T> Collection<T>.secondOrNull(): T? = if (size >= 2) elementAt(1) else null

/**
 * Returns the second element.
 *
 * @throws NoSuchElementException if the collection has fewer than 2 elements.
 */
public fun <T> Collection<T>.second(): T = secondOrNull() ?: throw NoSuchElementException("Collection has less than 2 elements")

/**
 * Returns a new list with the element at [index] replaced by [element].
 *
 * @param index zero-based position of the element to replace.
 * @param element the new value to place at [index].
 */
public fun <T> List<T>.updated(
    index: Int,
    element: T,
): List<T> = toMutableList().also { it[index] = element }

/**
 * Merges this map with [other], combining values for duplicate keys using [mergeValues].
 *
 * @param other the map to merge into this one.
 * @param mergeValues function that resolves a conflict between an existing value and the incoming value.
 */
public fun <K, V> Map<K, V>.mergeWith(
    other: Map<K, V>,
    mergeValues: (V, V) -> V,
): Map<K, V> {
    val result = toMutableMap()
    other.forEach { (k, v) -> result[k] = result[k]?.let { mergeValues(it, v) } ?: v }
    return result
}

/**
 * Splits this list into sublists, starting a new sublist each time [predicate] returns `true`.
 * The element that triggers the split becomes the first element of the new sublist.
 * Returns a list containing one empty list when the receiver is empty.
 *
 * @param predicate condition that triggers a split when satisfied.
 */
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
