package com.marcusprado02.commons.ports.email

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
