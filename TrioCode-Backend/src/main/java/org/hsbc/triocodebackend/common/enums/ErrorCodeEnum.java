package org.hsbc.triocodebackend.common.enums;

public enum ErrorCodeEnum {
    SUCCESS("SUCCESS", "Success", 200, false),

    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed.", 400, false),
    INVALID_AMOUNT("INVALID_AMOUNT", "Invalid amount.", 400, false),
    INVALID_ACCOUNT("INVALID_ACCOUNT", "Invalid account.", 400, false),
    INVALID_CURRENCY("INVALID_CURRENCY", "Unsupported currency.", 400, false),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Insufficient account balance.", 400, false),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "Invalid payment status transition.", 400, false),
    DUPLICATE_PAYMENT("DUPLICATE_PAYMENT", "Order number already exists. Please do not submit twice.", 409, false),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Payment record not found.", 404, false),

    PROCESSING_ERROR("PROCESSING_ERROR", "Payment processing error.", 500, true),
    NETWORK_ERROR("NETWORK_ERROR", "Network communication failed. Please try again later.", 503, true);

    private final String code;
    private final String message;
    private final int httpStatus;
    private final boolean retryable;

    private ErrorCodeEnum(String code, String message, int httpStatus, boolean retryable) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
