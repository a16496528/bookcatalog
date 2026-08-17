package com.example.bookcatalog.isbn;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class Isbn10ValidationStrategyTest {

    private final Isbn10ValidationStrategy strategy = new Isbn10ValidationStrategy();

    @Test
    void supportsTenCharacters() {
        assertThat(strategy.supportedLength()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0306406152", "097522980X"})
    void acceptsValidIsbn10(String isbn) {
        assertThat(strategy.isValid(isbn)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0306406153", "097522980x", "ABCDEFGHIJ", "03064061522"})
    void rejectsInvalidIsbn10(String isbn) {
        assertThat(strategy.isValid(isbn)).isFalse();
    }
}

