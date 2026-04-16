package com.marcusprado02.commons.ports.email

public data class EmailContent(
    val html: String? = null,
    val plain: String? = null,
) {
    init { require(html != null || plain != null) { "Email must have at least html or plain content" } }
}
