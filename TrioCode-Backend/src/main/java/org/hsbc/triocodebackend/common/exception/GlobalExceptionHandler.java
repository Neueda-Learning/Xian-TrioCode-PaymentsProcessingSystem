package org.hsbc.triocodebackend.common.exception;

import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BizException.class)
	public Result<Void> handleBizException(BizException ex, HttpServletResponse response) {
		ErrorCodeEnum errorCode = ex.getErrorCode();
		response.setStatus(errorCode.getHttpStatus());
		return Result.fail(errorCode, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<Void> handleUnexpectedException(Exception ex, HttpServletResponse response) {
		response.setStatus(ErrorCodeEnum.PROCESSING_ERROR.getHttpStatus());
		return Result.fail(ErrorCodeEnum.PROCESSING_ERROR);
	}
}
