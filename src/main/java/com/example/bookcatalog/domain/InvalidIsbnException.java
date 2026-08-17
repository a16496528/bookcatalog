package com.example.bookcatalog.domain;

public class InvalidIsbnException extends RuntimeException {

    public InvalidIsbnException(String message) {
        super(message);
    }
}

