package com.spring.resilience4j.exception;

public class DownstreamNotAvailableException extends RuntimeException {

    public DownstreamNotAvailableException(String message) {
        super(message);
    }
}
