package com.example.bookcatalog.application;

import com.example.bookcatalog.api.BookRequest;
import com.example.bookcatalog.api.BookResponse;
import com.example.bookcatalog.domain.Book;
import com.example.bookcatalog.domain.BookNotFoundException;
import com.example.bookcatalog.domain.IsbnConflictException;
import com.example.bookcatalog.isbn.IsbnService;
import com.example.bookcatalog.persistence.BookRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final IsbnService isbnService;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, IsbnService isbnService, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.isbnService = isbnService;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String canonicalIsbn = isbnService.validateAndNormalize(request.isbn());
        ensureIsbnAvailableForCreate(canonicalIsbn);
        Book book = bookMapper.toEntity(request, canonicalIsbn);
        return bookMapper.toResponse(saveOrThrowConflict(book, canonicalIsbn));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> list() {
        return bookRepository.findAllByOrderByIdAsc().stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse get(Long id) {
        return bookMapper.toResponse(findBook(id));
    }

    @Transactional
    public BookResponse replace(Long id, BookRequest request) {
        Book book = findBook(id);
        String canonicalIsbn = isbnService.validateAndNormalize(request.isbn());
        if (bookRepository.existsByIsbnAndIdNot(canonicalIsbn, id)) {
            throw new IsbnConflictException(canonicalIsbn);
        }
        bookMapper.update(book, request, canonicalIsbn);
        return bookMapper.toResponse(saveOrThrowConflict(book, canonicalIsbn));
    }

    @Transactional
    public void delete(Long id) {
        Book book = findBook(id);
        bookRepository.delete(book);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    private void ensureIsbnAvailableForCreate(String canonicalIsbn) {
        if (bookRepository.existsByIsbn(canonicalIsbn)) {
            throw new IsbnConflictException(canonicalIsbn);
        }
    }

    private Book saveOrThrowConflict(Book book, String canonicalIsbn) {
        try {
            return bookRepository.saveAndFlush(book);
        } catch (DataIntegrityViolationException exception) {
            throw new IsbnConflictException(canonicalIsbn);
        }
    }
}

