package com.rewards.api.exception;
/**
 * Exception thrown when transaction not found for the existing customer.
 */
public class TransactionNotFoundException extends RuntimeException{
    public TransactionNotFoundException(String message)
    {
        super(message);
    }
}
