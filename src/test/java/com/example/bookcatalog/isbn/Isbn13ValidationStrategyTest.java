package com.example.bookcatalog.isbn;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class Isbn13ValidationStrategyTest {

    private final Isbn13ValidationStrategy strategy = new Isbn13ValidationStrategy();

    @Test
    void supportsThirteenCharacters() {
        assertThat(strategy.supportedLength()).isEqualTo(13);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9780306406157", "9780132350884"})
    void acceptsValidIsbn13(String isbn) {
        assertThat(strategy.isValid(isbn)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"9780306406158", "978030640615X", "978030640615", "abcdefghijklm"})
    void rejectsInvalidIsbn13(String isbn) {
        assertThat(strategy.isValid(isbn)).isFalse();
    }
}

