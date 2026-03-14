package com.skiply.receipt_service.exception;

public class ServiceUnavailableException extends RuntimeException {

    private static final long serialVersionUID = -7852351015448978297L;

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
