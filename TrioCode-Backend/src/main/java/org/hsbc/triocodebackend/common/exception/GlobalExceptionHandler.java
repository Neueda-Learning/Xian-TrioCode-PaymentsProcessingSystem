package org.hsbc.triocodebackend.common.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.hsbc.triocodebackend.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BizException.class)
	public Result<Void> handleBizException(BizException ex, HttpServletResponse response) {
		ErrorCodeEnum errorCode = ex.getErrorCode();
		response.setStatus(errorCode.getHttpStatus());
		return Result.fail(errorCode, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpServletResponse response) {
		response.setStatus(ErrorCodeEnum.VALIDATION_FAILED.getHttpStatus());
		return Result.fail(ErrorCodeEnum.VALIDATION_FAILED, extractValidationMessage(ex.getBindingResult().getFieldError()));
	}

	@ExceptionHandler(BindException.class)
	public Result<Void> handleBindException(BindException ex, HttpServletResponse response) {
		response.setStatus(ErrorCodeEnum.VALIDATION_FAILED.getHttpStatus());
		return Result.fail(ErrorCodeEnum.VALIDATION_FAILED, extractValidationMessage(ex.getBindingResult().getFieldError()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpServletResponse response) {
		response.setStatus(ErrorCodeEnum.VALIDATION_FAILED.getHttpStatus());
		return Result.fail(ErrorCodeEnum.VALIDATION_FAILED, "The request body is invalid or malformed.");
	}

	@ExceptionHandler(Exception.class)
	public Result<Void> handleUnexpectedException(Exception ex, HttpServletResponse response) {
		logger.error("Unexpected system exception:", ex);
		response.setStatus(ErrorCodeEnum.PROCESSING_ERROR.getHttpStatus());
		return Result.fail(ErrorCodeEnum.PROCESSING_ERROR);
	}

	private String extractValidationMessage(FieldError fieldError) {
		if (fieldError == null) {
			return ErrorCodeEnum.VALIDATION_FAILED.getMessage();
		}
		String defaultMessage = fieldError.getDefaultMessage();
		return (defaultMessage == null || defaultMessage.isBlank())
				? ErrorCodeEnum.VALIDATION_FAILED.getMessage()
				: defaultMessage;
	}
}
