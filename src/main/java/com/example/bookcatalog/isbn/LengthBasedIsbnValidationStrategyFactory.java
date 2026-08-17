package com.example.bookcatalog.isbn;

import com.example.bookcatalog.domain.InvalidIsbnException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class LengthBasedIsbnValidationStrategyFactory implements IsbnValidationStrategyFactory {

    private final Map<Integer, IsbnValidationStrategy> strategiesByLength;

    public LengthBasedIsbnValidationStrategyFactory(List<IsbnValidationStrategy> strategies) {
        this.strategiesByLength = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        IsbnValidationStrategy::supportedLength,
                        Function.identity()));
    }

    @Override
    public IsbnValidationStrategy forIsbn(String normalizedIsbn) {
        int length = normalizedIsbn == null ? 0 : normalizedIsbn.length();
        IsbnValidationStrategy strategy = strategiesByLength.get(length);
        if (strategy == null) {
            throw new InvalidIsbnException("ISBN must contain 10 or 13 characters after normalization");
        }
        return strategy;
    }
}

