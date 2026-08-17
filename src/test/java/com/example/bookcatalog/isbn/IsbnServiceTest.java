package com.example.bookcatalog.isbn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.bookcatalog.domain.InvalidIsbnException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IsbnServiceTest {

    @Mock
    private IsbnValidationStrategyFactory factory;

    @Mock
    private IsbnValidationStrategy strategy;

    private IsbnService isbnService;

    @BeforeEach
    void setUp() {
        isbnService = new IsbnService(factory);
    }

    @Test
    void normalizesFormattingAndLowercaseTerminalXBeforeValidation() {
        when(factory.forIsbn("097522980X")).thenReturn(strategy);
        when(strategy.isValid("097522980X")).thenReturn(true);

        assertThat(isbnService.validateAndNormalize("0-97522 980-x")).isEqualTo("097522980X");
        verify(factory).forIsbn("097522980X");
        verify(strategy).isValid("097522980X");
    }

    @Test
    void rejectsFailedChecksum() {
        when(factory.forIsbn("0306406153")).thenReturn(strategy);
        when(strategy.isValid("0306406153")).thenReturn(false);

        assertThatThrownBy(() -> isbnService.validateAndNormalize("0306406153"))
                .isInstanceOf(InvalidIsbnException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void rejectsNullInput() {
        assertThatThrownBy(() -> isbnService.validateAndNormalize(null))
                .isInstanceOf(InvalidIsbnException.class)
                .hasMessageContaining("required");
    }
}
