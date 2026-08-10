package com.kopibru.bookmanagement.controller;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.exception.GlobalExceptionHandler;
import com.kopibru.bookmanagement.model.BookStatus;
import com.kopibru.bookmanagement.service.BookRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookRegistrationService bookRegistrationService;

    @Test
    void registerBook_returnsCreated() throws Exception {
        BookDto book = new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        when(bookRegistrationService.register(any(BookDto.class))).thenReturn(book);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("BOOK-001"));
    }

    @Test
    void registerBook_returnsBadRequestWhenInvalid() throws Exception {
        String body = """
                {"id":"","isbn":"","title":"","author":""}
                """;

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importFromFile_returnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "BOOK-001|978-1|Title|Author".getBytes());
        when(bookRegistrationService.readBooksFromFile(any())).thenReturn(List.of(
                new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null)));

        mockMvc.perform(multipart("/api/books/from-file").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("BOOK-001"));
    }
}
