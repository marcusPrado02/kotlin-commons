package com.marcusprado02.commons.kernel.errors

public object StandardErrorCodes {
    public val VALIDATION_ERROR: ErrorCode = ErrorCode("VALIDATION_ERROR")
    public val BUSINESS_ERROR: ErrorCode = ErrorCode("BUSINESS_ERROR")
    public val NOT_FOUND: ErrorCode = ErrorCode("NOT_FOUND")
    public val CONFLICT: ErrorCode = ErrorCode("CONFLICT")
    public val UNAUTHORIZED: ErrorCode = ErrorCode("UNAUTHORIZED")
    public val FORBIDDEN: ErrorCode = ErrorCode("FORBIDDEN")
    public val TECHNICAL_ERROR: ErrorCode = ErrorCode("TECHNICAL_ERROR")
}
