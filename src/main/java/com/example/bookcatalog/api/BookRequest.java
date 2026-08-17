package com.example.bookcatalog.api;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank(message = "title must not be blank") String title,
        @NotBlank(message = "author must not be blank") String author,
        @NotBlank(message = "isbn must not be blank") String isbn,
        Integer publicationYear) {
}

