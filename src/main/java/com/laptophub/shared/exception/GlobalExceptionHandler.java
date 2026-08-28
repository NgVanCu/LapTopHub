package com.laptophub.shared.exception;

import com.laptophub.shared.response.ErrorResponse;
import com.laptophub.shared.response.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================
    // App Exception
    // =========================================================

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                exception.getErrorCode(),
                request
        );
    }

    // =========================================================
    // Resource Not Found
    // =========================================================

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.RESOURCE_NOT_FOUND,
                request
        );
    }

    // =========================================================
    // Validation - @RequestBody
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<FieldErrorResponse> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        new FieldErrorResponse(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                )
                .toList();

        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                request,
                errors
        );
    }

    // =========================================================
    // Validation - @RequestParam / @PathVariable
    // =========================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {

        List<FieldErrorResponse> errors = exception
                .getConstraintViolations()
                .stream()
                .map(violation ->
                        new FieldErrorResponse(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .toList();

        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                request,
                errors
        );
    }

    // =========================================================
    // Validation - Handler Method
    // =========================================================

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {

        List<FieldErrorResponse> errors = exception
                .getParameterValidationResults()
                .stream()
                .flatMap(result ->
                        result.getResolvableErrors()
                                .stream()
                                .map(error ->
                                        new FieldErrorResponse(
                                                result.getMethodParameter()
                                                        .getParameterName(),
                                                error.getDefaultMessage()
                                        )
                                )
                )
                .toList();

        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                request,
                errors
        );
    }

    // =========================================================
    // Authentication
    // =========================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.INVALID_CREDENTIALS,
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.UNAUTHENTICATED,
                request
        );
    }

    // =========================================================
    // Authorization
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.ACCESS_DENIED,
                request
        );
    }

    // =========================================================
    // File Upload
    // =========================================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.FILE_TOO_LARGE,
                request
        );
    }

    // =========================================================
    // Invalid JSON Request Body
    // =========================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.BAD_REQUEST,
                request
        );
    }

    // =========================================================
    // Invalid Parameter Type
    // =========================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.BAD_REQUEST,
                request
        );
    }

    // =========================================================
    // Missing Request Parameter
    // =========================================================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.BAD_REQUEST,
                request
        );
    }

    // =========================================================
    // Unsupported HTTP Method
    // =========================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.BAD_REQUEST,
                request
        );
    }

    // =========================================================
    // Unsupported Content-Type
    // =========================================================

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.BAD_REQUEST,
                request
        );
    }

    // =========================================================
    // Invalid Pageable / Sort Property
    // =========================================================

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(
            PropertyReferenceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                request
        );
    }

    // =========================================================
    // Unexpected Exception
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error(
                "Unhandled exception: {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                ErrorCode.INTERNAL_ERROR,
                request
        );
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            HttpServletRequest request
    ) {

        return buildResponse(
                errorCode,
                request,
                Collections.emptyList()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            HttpServletRequest request,
            List<FieldErrorResponse> errors
    ) {

        ErrorResponse response = new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}