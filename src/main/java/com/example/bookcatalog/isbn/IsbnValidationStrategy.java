package com.example.bookcatalog.isbn;

public interface IsbnValidationStrategy {

    int supportedLength();

    boolean isValid(String normalizedIsbn);
}

