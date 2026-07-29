package org.hsbc.triocodebackend.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All business error codes used throughout the payment processing system.
 *
 * <p>Each constant carries:
 * <ul>
 *   <li>{@code code}        – the machine-readable business code returned in {@code Result.code}</li>
 *   <li>{@code message}     – a human-readable default description</li>
 *   <li>{@code httpStatus}  – the HTTP status code to use in the global exception handler</li>
 *   <li>{@code retryable}   – whether the client/system may safely retry the operation</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum {

    // ----------------------------------------------------------------
    // 2xx – Success
    // ----------------------------------------------------------------
    SUCCESS("SUCCESS", "Success", 200, false),

    // ----------------------------------------------------------------
    // 4xx – Client / business errors
    // ----------------------------------------------------------------
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed.", 400, false),
    INVALID_AMOUNT("INVALID_AMOUNT", "Invalid amount.", 400, false),
    INVALID_ACCOUNT("INVALID_ACCOUNT", "Invalid account.", 400, false),
    INVALID_CURRENCY("INVALID_CURRENCY", "Unsupported currency.", 400, false),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "Insufficient account balance.", 400, false),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "Invalid payment status transition.", 400, false),
    DUPLICATE_PAYMENT("DUPLICATE_PAYMENT", "Order number already exists. Please do not submit twice.", 409, false),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "Payment record not found.", 404, false),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Account not found.", 404, false),

    // ----------------------------------------------------------------
    // 5xx – Server / infrastructure errors
    // ----------------------------------------------------------------
    PROCESSING_ERROR("PROCESSING_ERROR", "Payment processing error.", 500, true),
    CLEARING_SYSTEM_UNAVAILABLE("CLEARING_SYSTEM_UNAVAILABLE", "The clearing system is temporarily unavailable.", 503, true),
    NETWORK_ERROR("NETWORK_ERROR", "Network communication failed. Please try again later.", 503, true);

    // ----------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------

    /** Machine-readable business code */
    private final String code;

    /** Default human-readable description */
    private final String message;

    /** Suggested HTTP response status */
    private final int httpStatus;

    /** Whether the caller may safely retry this error */
    private final boolean retryable;
}

