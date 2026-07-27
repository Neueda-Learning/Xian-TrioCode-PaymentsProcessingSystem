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
    SUCCESS("SUCCESS", "操作成功", 200, false),

    // ----------------------------------------------------------------
    // 4xx – Client / business errors
    // ----------------------------------------------------------------
    VALIDATION_FAILED("VALIDATION_FAILED", "参数校验失败", 400, false),
    INVALID_AMOUNT("INVALID_AMOUNT", "金额无效（为0/负数/超上限/精度错误）", 400, false),
    INVALID_ACCOUNT("INVALID_ACCOUNT", "账户不存在或已禁用", 400, false),
    INVALID_CURRENCY("INVALID_CURRENCY", "不支持的币种", 400, false),
    INSUFFICIENT_FUNDS("INSUFFICIENT_FUNDS", "账户余额不足", 400, false),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "非法的支付状态流转", 400, false),
    DUPLICATE_PAYMENT("DUPLICATE_PAYMENT", "订单号已存在，请勿重复提交", 409, false),
    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "支付记录不存在", 404, false),

    // ----------------------------------------------------------------
    // 5xx – Server / infrastructure errors
    // ----------------------------------------------------------------
    PROCESSING_ERROR("PROCESSING_ERROR", "系统内部处理异常", 500, true),
    NETWORK_ERROR("NETWORK_ERROR", "网络通信失败，请稍后重试", 503, true);

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

