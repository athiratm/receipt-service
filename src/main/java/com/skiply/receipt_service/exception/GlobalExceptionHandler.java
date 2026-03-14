package com.skiply.receipt_service.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * To handle ResourceNotFoundException for student not exists scenario
     *
     * @param ex The exception thrown when a resource is not found.
     * @param request The HTTP request that triggered the exception.
     * @return A response entity with a not found error.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * To handle ReceiptGenerationException for receipt generation failure scenario
     *
     * @param ex The exception thrown when receipt failed.
     * @param request The HTTP request that triggered the exception.
     * @return A response entity with a conflict error.
     */
    @ExceptionHandler(ReceiptGenerationException.class)
    public ResponseEntity<ApiErrorResponse> handleStudentAlreadyExists(ReceiptGenerationException ex, HttpServletRequest request) {
        logger.error("Receipt generation failed: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * To handle ServiceUnavailableException for microservice unavailable scenario
     *
     * @param ex The exception thrown when service down or unavailable.
     * @param request The HTTP request that triggered the exception.
     * @return A response entity with a conflict error.
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleStudentAlreadyExists(ServiceUnavailableException ex, HttpServletRequest request) {
        logger.error("Service Unavailable: {}", ex.getMessage());
        return createErrorResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * To handle all other exceptions
     *
     * @param ex The exception thrown.
     * @param request The HTTP request that triggered the exception.
     * @return A response entity with an internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(Exception ex,
                                                                  HttpServletRequest request) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method to define error object and error response entity
     *
     * @param message Exception message
     * @param status Http status code for exception
     * @return A resonse entity with error
     */
    private ResponseEntity<ApiErrorResponse> createErrorResponse(String message, HttpStatus status) {
        ApiErrorResponse error = new ApiErrorResponse(LocalDateTime.now(), status.value(), message);
        return new ResponseEntity<>(error, status);
    }
}