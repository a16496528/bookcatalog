package com.example.bookcatalog.isbn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.bookcatalog.domain.InvalidIsbnException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LengthBasedIsbnValidationStrategyFactoryTest {

    private Isbn10ValidationStrategy isbn10;
    private Isbn13ValidationStrategy isbn13;
    private LengthBasedIsbnValidationStrategyFactory factory;

    @BeforeEach
    void setUp() {
        isbn10 = new Isbn10ValidationStrategy();
        isbn13 = new Isbn13ValidationStrategy();
        factory = new LengthBasedIsbnValidationStrategyFactory(List.of(isbn10, isbn13));
    }

    @Test
    void selectsIsbn10Strategy() {
        assertThat(factory.forIsbn("0306406152")).isSameAs(isbn10);
    }

    @Test
    void selectsIsbn13Strategy() {
        assertThat(factory.forIsbn("9780132350884")).isSameAs(isbn13);
    }

    @Test
    void rejectsNullAndUnsupportedLengths() {
        assertThatThrownBy(() -> factory.forIsbn(null)).isInstanceOf(InvalidIsbnException.class);
        assertThatThrownBy(() -> factory.forIsbn("12345678901")).isInstanceOf(InvalidIsbnException.class);
    }
}

