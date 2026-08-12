package com.kopibru.bookmanagement.exception;

public class InvalidBookStatusException extends RuntimeException {

    public InvalidBookStatusException(String message) {
        super(message);
    }
}
