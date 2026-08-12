package com.kopibru.bookmanagement.service;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.dto.BorrowRequest;
import com.kopibru.bookmanagement.exception.BookNotAvailableException;
import com.kopibru.bookmanagement.exception.BookReturnException;
import com.kopibru.bookmanagement.exception.InvalidBookStatusException;
import com.kopibru.bookmanagement.exception.ResourceNotFoundException;
import com.kopibru.bookmanagement.model.BookCopy;
import com.kopibru.bookmanagement.model.BookDetail;
import com.kopibru.bookmanagement.model.BookStatus;
import com.kopibru.bookmanagement.repository.BookCopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class BookCatalogService {

    private final BookCopyRepository bookCopyRepository;

    public BookCatalogService(BookCopyRepository bookCopyRepository) {
        this.bookCopyRepository = bookCopyRepository;
    }

    public List<BookDto> getBooks(String statusFilter, boolean includeLibraryId) {
        List<BookCopy> copies = findCopiesByStatusFilter(statusFilter);
        return copies.stream()
                .map(copy -> toDto(copy, copy.getBookDetail(), includeLibraryId))
                .toList();
    }

    @Transactional
    public BookDto borrow(String bookId, BorrowRequest request) {
        BookCopy copy = bookCopyRepository.findByIdWithDetailForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book copy with id '" + bookId + "' not found"));

        if (copy.getStatus() != BookStatus.AVAILABLE) {
            throw new BookNotAvailableException(
                    "Book copy with id '" + bookId + "' is not available");
        }

        copy.setLibraryId(request.getLibraryId());
        copy.setStatus(BookStatus.BORROWED);
        BookCopy saved = bookCopyRepository.save(copy);
        return toDto(saved, saved.getBookDetail(), true);
    }

    @Transactional
    public BookDto returnBook(String bookId, BorrowRequest request) {
        BookCopy copy = bookCopyRepository.findByIdWithDetailForUpdate(bookId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book copy with id '" + bookId + "' not found"));

        if (copy.getStatus() != BookStatus.BORROWED
                || !request.getLibraryId().equals(copy.getLibraryId())) {
            throw new BookReturnException(
                    "Book copy with id '" + bookId
                            + "' is not borrowed by libraryId '" + request.getLibraryId() + "'");
        }

        copy.setLibraryId(null);
        copy.setStatus(BookStatus.AVAILABLE);
        BookCopy saved = bookCopyRepository.save(copy);
        return toDto(saved, saved.getBookDetail(), false);
    }

    private List<BookCopy> findCopiesByStatusFilter(String statusFilter) {
        String normalized = statusFilter == null ? "all" : statusFilter.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "all" -> bookCopyRepository.findAllWithDetail();
            case "available" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.AVAILABLE);
            case "borrowed" -> bookCopyRepository.findAllWithDetailByStatus(BookStatus.BORROWED);
            default -> throw new InvalidBookStatusException(
                    "Invalid status '" + statusFilter + "'. Allowed values: all, available, borrowed");
        };
    }

    private BookDto toDto(BookCopy copy, BookDetail detail, boolean includeLibraryId) {
        return new BookDto(
                copy.getId(),
                detail.getIsbn(),
                detail.getTitle(),
                detail.getAuthor(),
                copy.getStatus(),
                includeLibraryId ? copy.getLibraryId() : null);
    }
}
