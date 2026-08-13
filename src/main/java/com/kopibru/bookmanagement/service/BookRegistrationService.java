package com.kopibru.bookmanagement.service;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.exception.DuplicateBookException;
import com.kopibru.bookmanagement.model.BookCopy;
import com.kopibru.bookmanagement.model.BookDetail;
import com.kopibru.bookmanagement.model.BookStatus;
import com.kopibru.bookmanagement.repository.BookCopyRepository;
import com.kopibru.bookmanagement.repository.BookDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(BookRegistrationService.class);

    private final BookDetailRepository bookDetailRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookFileReader bookFileReader;

    public BookRegistrationService(
            BookDetailRepository bookDetailRepository,
            BookCopyRepository bookCopyRepository,
            BookFileReader bookFileReader) {
        this.bookDetailRepository = bookDetailRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.bookFileReader = bookFileReader;
    }

    @Transactional
    public BookDto register(BookDto book) {
        log.info("Registering book id={} isbn={}", book.getId(), book.getIsbn());
        if (bookCopyRepository.existsById(book.getId())) {
            throw new DuplicateBookException(
                    "Book copy with id '" + book.getId() + "' already exists");
        }

        BookDetail detail = bookDetailRepository.findById(book.getIsbn())
                .map(existing -> validateAndReuseDetail(existing, book))
                .orElseGet(() -> bookDetailRepository.save(
                        new BookDetail(book.getIsbn(), book.getTitle(), book.getAuthor())));

        BookStatus status = book.getStatus() != null ? book.getStatus() : BookStatus.AVAILABLE;
        BookCopy copy = bookCopyRepository.save(new BookCopy(book.getId(), detail, status));
        log.info("Registered book id={} isbn={} status={}", copy.getId(), detail.getIsbn(), copy.getStatus());
        return toDto(copy, detail);
    }

    @Transactional
    public List<BookDto> readBooksFromFile(MultipartFile file) {
        log.info("Importing books from file={}", file.getOriginalFilename());
        List<BookDto> booksFromFile = bookFileReader.read(file);
        List<BookDto> savedBooks = new ArrayList<>();
        for (BookDto book : booksFromFile) {
            savedBooks.add(register(book));
        }
        log.info("Imported {} books from file={}", savedBooks.size(), file.getOriginalFilename());
        return savedBooks;
    }

    private BookDetail validateAndReuseDetail(BookDetail existing, BookDto book) {
        if (!existing.getTitle().equals(book.getTitle())
                || !existing.getAuthor().equals(book.getAuthor())) {
            throw new DuplicateBookException(
                    "ISBN '" + book.getIsbn()
                            + "' already exists with a different title or author");
        }
        return existing;
    }

    private BookDto toDto(BookCopy copy, BookDetail detail) {
        return new BookDto(
                copy.getId(),
                detail.getIsbn(),
                detail.getTitle(),
                detail.getAuthor(),
                copy.getStatus(),
                null);
    }
}
