package com.example.bookcatalog.isbn;

import org.springframework.stereotype.Component;

@Component
public class Isbn13ValidationStrategy implements IsbnValidationStrategy {

    @Override
    public int supportedLength() {
        return 13;
    }

    @Override
    public boolean isValid(String normalizedIsbn) {
        if (normalizedIsbn == null || !normalizedIsbn.matches("[0-9]{13}")) {
            return false;
        }

        int sum = 0;
        for (int index = 0; index < normalizedIsbn.length(); index++) {
            int digit = normalizedIsbn.charAt(index) - '0';
            sum += digit * (index % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }
}

