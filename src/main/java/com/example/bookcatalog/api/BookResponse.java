package com.example.bookcatalog.api;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publicationYear) {
}

