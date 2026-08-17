package com.example.bookcatalog.isbn;

public interface IsbnValidationStrategyFactory {

    IsbnValidationStrategy forIsbn(String normalizedIsbn);
}

