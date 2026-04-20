package com.marcusprado02.commons.ports.email

/**
 * Body of an email message supporting HTML and/or plain-text representations.
 * At least one of [html] or [plain] must be provided.
 *
 * @property html optional HTML body.
 * @property plain optional plain-text body.
 */
public data class EmailContent(
    val html: String? = null,
    val plain: String? = null,
) {
    init {
        require(html != null || plain != null) { "Email must have at least html or plain content" }
    }

    public companion object {
        /**
         * Creates an [EmailContent] with both plain-text and HTML bodies.
         *
         * @param plain the plain-text version.
         * @param html the HTML version.
         */
        public fun withBoth(
            plain: String,
            html: String,
        ): EmailContent = EmailContent(html = html, plain = plain)
    }
}
