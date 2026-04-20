package com.marcusprado02.commons.ports.persistence

/**
 * Parameters for a paginated query.
 *
 * @property page zero-based page index (must be non-negative).
 * @property size maximum number of items per page (must be positive).
 * @property sort ordering criteria applied to the query.
 */
public data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sort: List<SortField> = emptyList(),
) {
    init {
        require(page >= 0) { "Page must be non-negative, was $page" }
        require(size > 0) { "Size must be positive, was $size" }
    }
}

/**
 * Ordering directive for a single field.
 *
 * @property field the field name to sort by.
 * @property direction sort direction; defaults to [SortDirection.ASC].
 */
public data class SortField(
    val field: String,
    val direction: SortDirection = SortDirection.ASC,
)

/** Sort order direction. */
public enum class SortDirection { ASC, DESC }
