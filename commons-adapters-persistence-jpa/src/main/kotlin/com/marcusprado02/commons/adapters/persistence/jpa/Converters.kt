package com.marcusprado02.commons.adapters.persistence.jpa

import com.marcusprado02.commons.ports.persistence.PageRequest
import com.marcusprado02.commons.ports.persistence.PageResult
import com.marcusprado02.commons.ports.persistence.SortDirection
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Page as SpringPage
import org.springframework.data.domain.PageRequest as SpringPageRequest

internal fun PageRequest.toSpringPageable(): Pageable =
    if (sort.isEmpty()) {
        SpringPageRequest.of(page, size)
    } else {
        val springSort =
            Sort.by(
                sort.map { sf ->
                    if (sf.direction == SortDirection.ASC) {
                        Sort.Order.asc(sf.field)
                    } else {
                        Sort.Order.desc(sf.field)
                    }
                },
            )
        SpringPageRequest.of(page, size, springSort)
    }

internal fun <E> SpringPage<E>.toPageResult(): PageResult<E> =
    PageResult(
        content = content,
        page = number,
        size = size,
        totalElements = totalElements,
    )
