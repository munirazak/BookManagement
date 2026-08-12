package com.kopibru.bookmanagement.controller;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.dto.BorrowRequest;
import com.kopibru.bookmanagement.service.BookCatalogService;
import com.kopibru.bookmanagement.service.BookRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRegistrationService bookRegistrationService;
    private final BookCatalogService bookCatalogService;

    public BookController(
            BookRegistrationService bookRegistrationService,
            BookCatalogService bookCatalogService) {
        this.bookRegistrationService = bookRegistrationService;
        this.bookCatalogService = bookCatalogService;
    }

    @PostMapping
    public ResponseEntity<BookDto> registerBook(@Valid @RequestBody BookDto book) {
        BookDto registered = bookRegistrationService.register(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @PostMapping(value = "/from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BookDto>> readBooksFromFile(@RequestParam("file") MultipartFile file) {
        List<BookDto> savedBooks = bookRegistrationService.readBooksFromFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooks);
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "false") boolean includeLibraryId) {
        return ResponseEntity.ok(bookCatalogService.getBooks(status, includeLibraryId));
    }

    @PostMapping("/{bookId}/borrow")
    public ResponseEntity<BookDto> borrowBook(
            @PathVariable String bookId,
            @Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.ok(bookCatalogService.borrow(bookId, request));
    }

    @PostMapping("/{bookId}/return")
    public ResponseEntity<BookDto> returnBook(
            @PathVariable String bookId,
            @Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.ok(bookCatalogService.returnBook(bookId, request));
    }
}
