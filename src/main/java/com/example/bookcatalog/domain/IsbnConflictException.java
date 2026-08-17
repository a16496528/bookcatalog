package com.example.bookcatalog.domain;

public class IsbnConflictException extends RuntimeException {

    public IsbnConflictException(String isbn) {
        super("A book with ISBN " + isbn + " already exists");
    }
}

