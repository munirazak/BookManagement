package com.kopibru.bookmanagement.controller;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.dto.BorrowRequest;
import com.kopibru.bookmanagement.service.BookCatalogService;
import com.kopibru.bookmanagement.service.BookRegistrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

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
        log.info("POST /api/books id={} isbn={}", book.getId(), book.getIsbn());
        BookDto registered = bookRegistrationService.register(book);
        log.info("Book registered id={} isbn={} status={}", registered.getId(), registered.getIsbn(), registered.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @PostMapping(value = "/from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BookDto>> readBooksFromFile(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/books/from-file filename={}", file.getOriginalFilename());
        List<BookDto> savedBooks = bookRegistrationService.readBooksFromFile(file);
        log.info("Imported {} books from file={}", savedBooks.size(), file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooks);
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "false") boolean includeLibraryId) {
        log.info("GET /api/books status={} includeLibraryId={}", status, includeLibraryId);
        List<BookDto> books = bookCatalogService.getBooks(status, includeLibraryId);
        log.info("Returning {} books for status={}", books.size(), status);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/{bookId}/borrow")
    public ResponseEntity<BookDto> borrowBook(
            @PathVariable String bookId,
            @Valid @RequestBody BorrowRequest request) {
        log.info("POST /api/books/{}/borrow libraryId={}", bookId, request.getLibraryId());
        BookDto result = bookCatalogService.borrow(bookId, request);
        log.info("Borrow completed bookId={} libraryId={} status={}", bookId, request.getLibraryId(), result.getStatus());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{bookId}/return")
    public ResponseEntity<BookDto> returnBook(
            @PathVariable String bookId,
            @Valid @RequestBody BorrowRequest request) {
        log.info("POST /api/books/{}/return libraryId={}", bookId, request.getLibraryId());
        BookDto result = bookCatalogService.returnBook(bookId, request);
        log.info("Return completed bookId={} libraryId={} status={}", bookId, request.getLibraryId(), result.getStatus());
        return ResponseEntity.ok(result);
    }
}
