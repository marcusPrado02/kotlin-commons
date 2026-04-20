package com.marcusprado02.commons.ports.email

/**
 * A file attachment to include in an [Email].
 *
 * @property filename the name of the file as it appears to the recipient.
 * @property content raw byte content of the attachment.
 * @property mimeType MIME type of the content (e.g. `"application/pdf"`).
 */
public data class EmailAttachment(
    val filename: String,
    val content: ByteArray,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmailAttachment) return false
        return filename == other.filename && content.contentEquals(other.content) && mimeType == other.mimeType
    }

    override fun hashCode(): Int = 31 * filename.hashCode() + content.contentHashCode() + mimeType.hashCode()
}
