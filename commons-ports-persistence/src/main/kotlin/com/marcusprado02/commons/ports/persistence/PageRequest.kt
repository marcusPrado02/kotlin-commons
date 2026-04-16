package com.marcusprado02.commons.ports.persistence

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

public data class SortField(
    val field: String,
    val direction: SortDirection = SortDirection.ASC,
)

public enum class SortDirection { ASC, DESC }
