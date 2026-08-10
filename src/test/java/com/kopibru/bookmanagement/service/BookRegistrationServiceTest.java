package com.kopibru.bookmanagement.service;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.exception.DuplicateBookException;
import com.kopibru.bookmanagement.model.BookCopy;
import com.kopibru.bookmanagement.model.BookDetail;
import com.kopibru.bookmanagement.model.BookStatus;
import com.kopibru.bookmanagement.repository.BookCopyRepository;
import com.kopibru.bookmanagement.repository.BookDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookRegistrationServiceTest {

    @Mock
    private BookDetailRepository bookDetailRepository;
    @Mock
    private BookCopyRepository bookCopyRepository;
    @Mock
    private BookFileReader bookFileReader;

    @InjectMocks
    private BookRegistrationService bookRegistrationService;

    @Test
    void register_createsDetailAndCopy() {
        BookDto request = new BookDto("BOOK-001", "978-1", "Title", "Author", null, null);
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);

        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.empty());
        when(bookDetailRepository.save(any(BookDetail.class))).thenReturn(detail);
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        BookDto result = bookRegistrationService.register(request);

        assertThat(result.getId()).isEqualTo("BOOK-001");
        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        verify(bookDetailRepository).save(any(BookDetail.class));
    }

    @Test
    void register_reusesDetailWhenIsbnMatches() {
        BookDto request = new BookDto("BOOK-002", "978-1", "Title", "Author", null, null);
        BookDetail existing = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-002", existing, BookStatus.AVAILABLE);

        when(bookCopyRepository.existsById("BOOK-002")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.of(existing));
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        BookDto result = bookRegistrationService.register(request);

        assertThat(result.getId()).isEqualTo("BOOK-002");
        verify(bookDetailRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenCopyIdExists() {
        BookDto request = new BookDto("BOOK-001", "978-1", "Title", "Author", null, null);
        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(true);

        assertThatThrownBy(() -> bookRegistrationService.register(request))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("BOOK-001");
    }

    @Test
    void register_throwsWhenIsbnHasDifferentTitleOrAuthor() {
        BookDto request = new BookDto("BOOK-002", "978-1", "Other Title", "Author", null, null);
        BookDetail existing = new BookDetail("978-1", "Title", "Author");

        when(bookCopyRepository.existsById("BOOK-002")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookRegistrationService.register(request))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("978-1");
    }

    @Test
    void readBooksFromFile_registersParsedBooks() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "data".getBytes());
        BookDto parsed = new BookDto("BOOK-001", "978-1", "Title", "Author", BookStatus.AVAILABLE, null);
        BookDetail detail = new BookDetail("978-1", "Title", "Author");
        BookCopy copy = new BookCopy("BOOK-001", detail, BookStatus.AVAILABLE);

        when(bookFileReader.read(file)).thenReturn(List.of(parsed));
        when(bookCopyRepository.existsById("BOOK-001")).thenReturn(false);
        when(bookDetailRepository.findById("978-1")).thenReturn(Optional.empty());
        when(bookDetailRepository.save(any(BookDetail.class))).thenReturn(detail);
        when(bookCopyRepository.save(any(BookCopy.class))).thenReturn(copy);

        List<BookDto> result = bookRegistrationService.readBooksFromFile(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("BOOK-001");
    }
}
