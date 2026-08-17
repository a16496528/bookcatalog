package com.example.bookcatalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookcatalog.api.BookRequest;
import com.example.bookcatalog.api.BookResponse;
import com.example.bookcatalog.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    void mapsRequestToEntityWithCanonicalIsbn() {
        BookRequest request = new BookRequest("Clean Code", "Robert C. Martin", "formatted", 2008);

        Book book = mapper.toEntity(request, "9780132350884");

        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getIsbn()).isEqualTo("9780132350884");
        assertThat(book.getPublicationYear()).isEqualTo(2008);
    }

    @Test
    void replacementPreservesIdentifierAndMapsExactPublicFields() {
        Book book = new Book("Old", "Old Author", "0306406152", 1980);
        ReflectionTestUtils.setField(book, "id", 42L);
        BookRequest request = new BookRequest("New", "New Author", "formatted", 2024);

        mapper.update(book, request, "9780132350884");
        BookResponse response = mapper.toResponse(book);

        assertThat(response).isEqualTo(new BookResponse(42L, "New", "New Author", "9780132350884", 2024));
    }
}

