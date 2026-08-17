package com.example.bookcatalog.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void queriesCanonicalIsbnAndListsByAscendingId() {
        Book first = bookRepository.saveAndFlush(new Book("First", "Author A", "0306406152", 1980));
        Book second = bookRepository.saveAndFlush(new Book("Second", "Author B", "9780132350884", 2008));

        assertThat(bookRepository.existsByIsbn("0306406152")).isTrue();
        assertThat(bookRepository.existsByIsbnAndIdNot("0306406152", first.getId())).isFalse();
        assertThat(bookRepository.existsByIsbnAndIdNot("0306406152", second.getId())).isTrue();
        assertThat(bookRepository.findAllByOrderByIdAsc())
                .extracting(Book::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void databaseRejectsDuplicateIsbn() {
        bookRepository.saveAndFlush(new Book("First", "Author A", "9780132350884", 2008));

        assertThatThrownBy(() -> bookRepository.saveAndFlush(
                new Book("Duplicate", "Author B", "9780132350884", 2020)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

