package com.marcusprado02.commons.ports.persistence

public data class PageResult<E>(
    val content: List<E>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    public val totalPages: Int = if (size == 0) 0 else ((totalElements + size - 1) / size).toInt()
    public val isFirst: Boolean = page == 0
    public val isLast: Boolean = page >= totalPages - 1
    public val isEmpty: Boolean = content.isEmpty()
}
