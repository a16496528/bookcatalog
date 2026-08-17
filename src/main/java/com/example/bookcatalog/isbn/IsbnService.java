package com.example.bookcatalog.isbn;

import com.example.bookcatalog.domain.InvalidIsbnException;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class IsbnService {

    private final IsbnValidationStrategyFactory strategyFactory;

    public IsbnService(IsbnValidationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public String validateAndNormalize(String isbn) {
        if (isbn == null) {
            throw new InvalidIsbnException("ISBN is required");
        }
        String normalized = isbn.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        IsbnValidationStrategy strategy = strategyFactory.forIsbn(normalized);
        if (!strategy.isValid(normalized)) {
            throw new InvalidIsbnException("ISBN checksum or format is invalid");
        }
        return normalized;
    }
}

