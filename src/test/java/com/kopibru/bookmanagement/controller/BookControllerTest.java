package com.kopibru.bookmanagement.controller;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.dto.BorrowRequest;
import com.kopibru.bookmanagement.exception.GlobalExceptionHandler;
import com.kopibru.bookmanagement.model.BookStatus;
import com.kopibru.bookmanagement.service.BookCatalogService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private BookCatalogService bookCatalogService;

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
    void getBooks_returnsOk() throws Exception {
        when(bookCatalogService.getBooks(eq("available"), anyBoolean())).thenReturn(List.of(
                new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null)));

        mockMvc.perform(get("/api/books").param("status", "available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void borrowBook_returnsOk() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.BORROWED, "LIB-001");
        when(bookCatalogService.borrow(eq("BOOK-001"), any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/BOOK-001/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libraryId").value("LIB-001"));
    }

    @Test
    void returnBook_returnsOk() throws Exception {
        BorrowRequest request = new BorrowRequest("LIB-001");
        BookDto response = new BookDto(
                "BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        when(bookCatalogService.returnBook(eq("BOOK-001"), any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books/BOOK-001/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
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
