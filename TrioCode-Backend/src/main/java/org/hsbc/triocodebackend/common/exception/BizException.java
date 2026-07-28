package org.hsbc.triocodebackend.common.exception;

import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;

public class BizException extends RuntimeException {
    private final ErrorCodeEnum errorCode;

    public BizException(ErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodeEnum getErrorCode() {
        return errorCode;
    }
}
