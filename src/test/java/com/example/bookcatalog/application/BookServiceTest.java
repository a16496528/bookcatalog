package com.example.bookcatalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.api.BookRequest;
import com.example.bookcatalog.api.BookResponse;
import com.example.bookcatalog.domain.Book;
import com.example.bookcatalog.domain.BookNotFoundException;
import com.example.bookcatalog.domain.IsbnConflictException;
import com.example.bookcatalog.isbn.IsbnService;
import com.example.bookcatalog.persistence.BookRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String ISBN = "9780132350884";

    @Mock
    private BookRepository repository;

    @Mock
    private IsbnService isbnService;

    @Mock
    private BookMapper mapper;

    private BookService service;
    private BookRequest request;

    @BeforeEach
    void setUp() {
        service = new BookService(repository, isbnService, mapper);
        request = new BookRequest("Clean Code", "Robert C. Martin", "978-0-13-235088-4", 2008);
    }

    @Test
    void createsBookAfterNormalizationAndUniquenessCheck() {
        Book book = new Book(request.title(), request.author(), ISBN, request.publicationYear());
        BookResponse response = new BookResponse(1L, request.title(), request.author(), ISBN, 2008);
        when(isbnService.validateAndNormalize(request.isbn())).thenReturn(ISBN);
        when(repository.existsByIsbn(ISBN)).thenReturn(false);
        when(mapper.toEntity(request, ISBN)).thenReturn(book);
        when(repository.saveAndFlush(book)).thenReturn(book);
        when(mapper.toResponse(book)).thenReturn(response);

        assertThat(service.create(request)).isEqualTo(response);
    }

    @Test
    void rejectsDuplicateBeforeCreatingEntity() {
        when(isbnService.validateAndNormalize(request.isbn())).thenReturn(ISBN);
        when(repository.existsByIsbn(ISBN)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IsbnConflictException.class);
        verifyNoInteractions(mapper);
    }

    @Test
    void translatesDatabaseUniquenessRace() {
        Book book = new Book(request.title(), request.author(), ISBN, request.publicationYear());
        when(isbnService.validateAndNormalize(request.isbn())).thenReturn(ISBN);
        when(repository.existsByIsbn(ISBN)).thenReturn(false);
        when(mapper.toEntity(request, ISBN)).thenReturn(book);
        when(repository.saveAndFlush(book)).thenThrow(new DataIntegrityViolationException("unique"));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(IsbnConflictException.class);
    }

    @Test
    void listsRepositoryResultsInRepositoryOrder() {
        Book first = new Book("A", "Author", "0306406152", null);
        Book second = new Book("B", "Author", ISBN, 2008);
        BookResponse firstResponse = new BookResponse(1L, "A", "Author", "0306406152", null);
        BookResponse secondResponse = new BookResponse(2L, "B", "Author", ISBN, 2008);
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(first, second));
        when(mapper.toResponse(first)).thenReturn(firstResponse);
        when(mapper.toResponse(second)).thenReturn(secondResponse);

        assertThat(service.list()).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void getsExistingBook() {
        Book book = new Book("A", "Author", "0306406152", null);
        BookResponse response = new BookResponse(1L, "A", "Author", "0306406152", null);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(mapper.toResponse(book)).thenReturn(response);

        assertThat(service.get(1L)).isEqualTo(response);
    }

    @Test
    void getRejectsMissingBook() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void replacesExistingBook() {
        Book book = new Book("Old", "Author", "0306406152", 1980);
        BookResponse response = new BookResponse(1L, request.title(), request.author(), ISBN, 2008);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(isbnService.validateAndNormalize(request.isbn())).thenReturn(ISBN);
        when(repository.existsByIsbnAndIdNot(ISBN, 1L)).thenReturn(false);
        when(repository.saveAndFlush(book)).thenReturn(book);
        when(mapper.toResponse(book)).thenReturn(response);

        assertThat(service.replace(1L, request)).isEqualTo(response);
        verify(mapper).update(book, request, ISBN);
    }

    @Test
    void replacementChecksExistenceBeforeValidation() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replace(99L, request)).isInstanceOf(BookNotFoundException.class);
        verifyNoInteractions(isbnService);
    }

    @Test
    void duplicateReplacementLeavesEntityUnchanged() {
        Book book = new Book("Old", "Author", "0306406152", 1980);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(isbnService.validateAndNormalize(request.isbn())).thenReturn(ISBN);
        when(repository.existsByIsbnAndIdNot(ISBN, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.replace(1L, request)).isInstanceOf(IsbnConflictException.class);
        assertThat(book.getTitle()).isEqualTo("Old");
        assertThat(book.getIsbn()).isEqualTo("0306406152");
        verify(mapper, never()).update(book, request, ISBN);
    }

    @Test
    void deletesExistingBook() {
        Book book = new Book("A", "Author", "0306406152", null);
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.delete(1L);

        verify(repository).delete(book);
    }

    @Test
    void deleteRejectsMissingBook() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(BookNotFoundException.class);
    }
}
