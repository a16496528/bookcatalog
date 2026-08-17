package com.example.bookcatalog.api;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.persistence.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void clearDatabase() {
        bookRepository.deleteAll();
    }

    @Test
    void exercisesCompleteCrudLifecycleAndOrderedListing() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));

        BookResponse first = createBook(new BookRequest(
                "Clean Code", "Robert C. Martin", "978-0-13-235088-4", 2008));
        BookResponse second = createBook(new BookRequest(
                "The C Programming Language", "Kernighan and Ritchie", "0-306-40615-2", 1988));

        mockMvc.perform(get("/api/books/{id}", first.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(5)))
                .andExpect(jsonPath("$.id").value(first.id()))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));

        BookRequest replacement = new BookRequest(
                "Clean Code, Second Edition", "Robert C. Martin", "9780132350884", 2025);
        mockMvc.perform(put("/api/books/{id}", first.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(replacement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(first.id()))
                .andExpect(jsonPath("$.title").value("Clean Code, Second Edition"))
                .andExpect(jsonPath("$.publicationYear").value(2025));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(first.id()))
                .andExpect(jsonPath("$[1].id").value(second.id()));

        mockMvc.perform(delete("/api/books/{id}", second.id()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/books/{id}", second.id()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsStandardFieldErrorsForBlankRequiredFieldsAndInvalidIsbn() throws Exception {
        BookRequest blank = new BookRequest(" ", "", " ", null);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(blank)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/books"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.author").exists())
                .andExpect(jsonPath("$.fieldErrors.isbn").exists());

        BookRequest invalidIsbn = new BookRequest("Book", "Author", "9780132350885", 2024);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(invalidIsbn)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(1)))
                .andExpect(jsonPath("$.fieldErrors.isbn").value("ISBN checksum or format is invalid"));

        BookRequest malformedIsbn = new BookRequest("Book", "Author", "not-an-isbn", 2024);
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(malformedIsbn)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.isbn").exists());
    }

    @Test
    void rejectsInvalidReplacementWithoutChangingStoredBook() throws Exception {
        BookResponse existing = createBook(new BookRequest("Original", "Original Author", "0306406152", 1980));
        BookRequest invalid = new BookRequest("Changed", "Changed Author", "0306406153", 2025);

        mockMvc.perform(put("/api/books/{id}", existing.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.isbn").exists());

        mockMvc.perform(get("/api/books/{id}", existing.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original"))
                .andExpect(jsonPath("$.author").value("Original Author"))
                .andExpect(jsonPath("$.isbn").value("0306406152"))
                .andExpect(jsonPath("$.publicationYear").value(1980));
    }

    @Test
    void returnsStandardBadRequestForMalformedJsonUnknownFieldsAndBadPathId() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/books"))
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":99,"title":"Book","author":"Author","isbn":"0306406152"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        mockMvc.perform(get("/api/books/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsStandardNotFoundForEveryItemMutation() throws Exception {
        BookRequest request = new BookRequest("Book", "Author", "0306406152", null);

        mockMvc.perform(get("/api/books/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Book with id 99999 was not found"))
                .andExpect(jsonPath("$.path").value("/api/books/99999"))
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        mockMvc.perform(put("/api/books/{id}", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        mockMvc.perform(delete("/api/books/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));
    }

    @Test
    void rejectsNormalizedCreateAndReplacementConflictsWithoutChangingStoredData() throws Exception {
        BookResponse first = createBook(new BookRequest("First", "Author A", "9780132350884", 2008));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new BookRequest("Duplicate", "Author B", "978-0-13-235088-4", 2020))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A book with ISBN 9780132350884 already exists"))
                .andExpect(jsonPath("$.path").value("/api/books"))
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        BookResponse second = createBook(new BookRequest("Second", "Author B", "0306406152", 1980));
        mockMvc.perform(put("/api/books/{id}", second.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new BookRequest("Changed", "Changed", "978-0-13-235088-4", 2025))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(0)));

        mockMvc.perform(get("/api/books/{id}", second.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Second"))
                .andExpect(jsonPath("$.isbn").value("0306406152"));

        mockMvc.perform(get("/api/books/{id}", first.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First"));
    }

    private BookResponse createBook(BookRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), BookResponse.class);
        String location = result.getResponse().getHeader("Location");
        org.assertj.core.api.Assertions.assertThat(location).endsWith("/api/books/" + response.id());
        return response;
    }
}
