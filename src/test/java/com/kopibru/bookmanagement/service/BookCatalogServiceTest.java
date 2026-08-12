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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCatalogServiceTest {

    @Mock
    private BookCopyRepository bookCopyRepository;

    @InjectMocks
    private BookCatalogService bookCatalogService;

    @Test
    void getBooks_hidesLibraryIdByDefault() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        copy.setLibraryId("LIB-001");
        when(bookCopyRepository.findAllWithDetail()).thenReturn(List.of(copy));

        List<BookDto> result = bookCatalogService.getBooks("all", false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLibraryId()).isNull();
    }

    @Test
    void getBooks_throwsForInvalidStatus() {
        assertThatThrownBy(() -> bookCatalogService.getBooks("missing", false))
                .isInstanceOf(InvalidBookStatusException.class);
    }

    @Test
    void borrow_succeedsWhenAvailable() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));
        when(bookCopyRepository.save(copy)).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookCatalogService.borrow("BOOK-001", new BorrowRequest("LIB-001"));

        assertThat(result.getStatus()).isEqualTo(BookStatus.BORROWED);
        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
    }

    @Test
    void borrow_throwsWhenMissing() {
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookCatalogService.borrow("BOOK-001", new BorrowRequest("LIB-001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void borrow_throwsWhenNotAvailable() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> bookCatalogService.borrow("BOOK-001", new BorrowRequest("LIB-001")))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void returnBook_succeedsWhenBorrowedBySameBorrower() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.BORROWED);
        copy.setLibraryId("LIB-001");
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));
        when(bookCopyRepository.save(copy)).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookCatalogService.returnBook("BOOK-001", new BorrowRequest("LIB-001"));

        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(result.getLibraryId()).isNull();
    }

    @Test
    void returnBook_throwsWhenNotBorrowedByBorrower() {
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);
        when(bookCopyRepository.findByIdWithDetailForUpdate("BOOK-001")).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> bookCatalogService.returnBook("BOOK-001", new BorrowRequest("LIB-001")))
                .isInstanceOf(BookReturnException.class);
    }
}
