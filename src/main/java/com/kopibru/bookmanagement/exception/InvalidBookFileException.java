package com.kopibru.bookmanagement.exception;

public class InvalidBookFileException extends RuntimeException {

    public InvalidBookFileException(String message) {
        super(message);
    }

    public InvalidBookFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
