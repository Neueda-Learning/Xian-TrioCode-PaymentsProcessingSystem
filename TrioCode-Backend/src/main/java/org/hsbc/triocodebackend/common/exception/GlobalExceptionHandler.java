package org.hsbc.triocodebackend.common.exception;

import org.hsbc.triocodebackend.common.result.Result;
import org.hsbc.triocodebackend.common.enums.ErrorCodeEnum;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

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
	public Result<Void> handleValidationException(MethodArgumentNotValidException ex, HttpServletResponse response) {
		response.setStatus(400);
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return Result.fail(ErrorCodeEnum.VALIDATION_FAILED, message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public Result<Void> handleConstraintViolation(ConstraintViolationException ex, HttpServletResponse response) {
		response.setStatus(400);
		String message = ex.getConstraintViolations().stream()
				.map(v -> v.getMessage())
				.collect(Collectors.joining("; "));
		return Result.fail(ErrorCodeEnum.VALIDATION_FAILED, message);
	}

	@ExceptionHandler(Exception.class)
	public Result<Void> handleUnexpectedException(Exception ex, HttpServletResponse response) {
		logger.error("系统异常：", ex);
		response.setStatus(ErrorCodeEnum.PROCESSING_ERROR.getHttpStatus());
		return Result.fail(ErrorCodeEnum.PROCESSING_ERROR);
	}
}
