package com.example.bookcatalog.isbn;

import org.springframework.stereotype.Component;

@Component
public class Isbn10ValidationStrategy implements IsbnValidationStrategy {

    @Override
    public int supportedLength() {
        return 10;
    }

    @Override
    public boolean isValid(String normalizedIsbn) {
        if (normalizedIsbn == null || !normalizedIsbn.matches("[0-9]{9}[0-9X]")) {
            return false;
        }

        int sum = 0;
        for (int index = 0; index < 9; index++) {
            sum += (normalizedIsbn.charAt(index) - '0') * (10 - index);
        }
        int checkDigit = normalizedIsbn.charAt(9) == 'X'
                ? 10
                : normalizedIsbn.charAt(9) - '0';
        return (sum + checkDigit) % 11 == 0;
    }
}

