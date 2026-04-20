package com.marcusprado02.commons.ports.persistence

/**
 * A single page of query results.
 *
 * @param E the element type.
 * @property content the items on this page.
 * @property page the zero-based index of this page.
 * @property size the requested page size.
 * @property totalElements the total number of elements across all pages.
 */
public data class PageResult<E>(
    val content: List<E>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    init {
        require(totalElements >= 0) { "totalElements must be non-negative, was $totalElements" }
    }

    /** Total number of pages given [size] and [totalElements]. */
    public val totalPages: Int = if (size == 0) 0 else ((totalElements + size - 1) / size).toInt()

    /** `true` if this is the first page (index 0). */
    public val isFirst: Boolean = page == 0

    /** `true` if this is the last page. */
    public val isLast: Boolean = page >= totalPages - 1

    /** `true` if [content] is empty. */
    public val isEmpty: Boolean = content.isEmpty()
}
