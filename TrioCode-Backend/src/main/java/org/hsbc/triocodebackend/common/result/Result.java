package org.hsbc.triocodebackend.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;

/**
 * Unified API response wrapper.
 *
 * @param <T> type of the business data payload
 */
@Data
@NoArgsConstructor
public class Result<T> {

    /** Business code, e.g. SUCCESS / VALIDATION_FAILED */
    private String code;

    /** Human-readable description */
    private String message;

    /** Business data payload; null on failure */
    private T data;

    // ----------------------------------------------------------------
    // Private constructor
    // ----------------------------------------------------------------

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ----------------------------------------------------------------
    // Static factory methods
    // ----------------------------------------------------------------

    /**
     * Successful response with data.
     *
     * @param data business payload
     * @param <T>  payload type
     * @return Result with code=SUCCESS
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(),
                ErrorCodeEnum.SUCCESS.getMessage(),
                data);
    }

    /**
     * Successful response without data (e.g. void operations).
     *
     * @param <T> payload type
     * @return Result with code=SUCCESS and null data
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * Failure response derived from an {@link ErrorCodeEnum}.
     *
     * @param errorCode error code enum value
     * @param <T>       payload type
     * @return Result with code and message from the enum, data=null
     */
    public static <T> Result<T> fail(ErrorCodeEnum errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * Failure response with extra data (e.g. partial results or diagnostic info).
     *
     * @param errorCode error code enum value
     * @param data      optional attached data
     * @param <T>       payload type
     * @return Result with code and message from the enum, and the supplied data
     */
    public static <T> Result<T> fail(ErrorCodeEnum errorCode, T data) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), data);
    }

    /**
     * Failure response with a custom message override (e.g. for dynamic validation messages).
     *
     * @param errorCode      error code enum value (provides the code)
     * @param customMessage  overrides the default enum message
     * @param <T>            payload type
     * @return Result with code from enum but a custom message
     */
    public static <T> Result<T> fail(ErrorCodeEnum errorCode, String customMessage) {
        return new Result<>(errorCode.getCode(), customMessage, null);
    }
}

