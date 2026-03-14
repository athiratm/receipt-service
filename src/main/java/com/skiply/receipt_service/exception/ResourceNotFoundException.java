package com.skiply.receipt_service.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = -7852351015448970297L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
