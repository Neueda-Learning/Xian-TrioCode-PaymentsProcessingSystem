package org.hsbc.triocodebackend.common.exception;
import lombok.Getter;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;

@Getter
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

}
