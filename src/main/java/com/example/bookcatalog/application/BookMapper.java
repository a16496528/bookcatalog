package com.example.bookcatalog.application;

import com.example.bookcatalog.api.BookRequest;
import com.example.bookcatalog.api.BookResponse;
import com.example.bookcatalog.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(BookRequest request, String canonicalIsbn) {
        return new Book(request.title(), request.author(), canonicalIsbn, request.publicationYear());
    }

    public void update(Book book, BookRequest request, String canonicalIsbn) {
        book.replaceDetails(request.title(), request.author(), canonicalIsbn, request.publicationYear());
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear());
    }
}

