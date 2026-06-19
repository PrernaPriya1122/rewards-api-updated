package com.rewards.api.exception;

/**
 * Exception thrown when a customer cannot be found.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
