package com.kopibru.bookmanagement.service;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.exception.InvalidBookFileException;
import com.kopibru.bookmanagement.model.BookStatus;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookFileReaderTest {

    private final BookFileReader bookFileReader = new BookFileReader();

    @Test
    void read_parsesValidTxtFile() {
        String content = """
                # comment
                id|isbn|title|author
                BOOK-001|978-1|Effective Java|Joshua Bloch
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample-books.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        List<BookDto> books = bookFileReader.read(file);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getId()).isEqualTo("BOOK-001");
        assertThat(books.get(0).getStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    void read_acceptsDatExtension() {
        String content = "BOOK-001|978-1|Title|Author\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.dat", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        assertThat(bookFileReader.read(file)).hasSize(1);
    }

    @Test
    void read_throwsWhenFileEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> bookFileReader.read(file))
                .isInstanceOf(InvalidBookFileException.class)
                .hasMessageContaining("required");
    }

    @Test
    void read_throwsWhenExtensionInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.csv", "text/plain", "BOOK-001|978-1|Title|Author".getBytes());

        assertThatThrownBy(() -> bookFileReader.read(file))
                .isInstanceOf(InvalidBookFileException.class)
                .hasMessageContaining(".txt or .dat");
    }

    @Test
    void read_throwsWhenLineInvalid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "BOOK-001|only-two".getBytes());

        assertThatThrownBy(() -> bookFileReader.read(file))
                .isInstanceOf(InvalidBookFileException.class)
                .hasMessageContaining("line 1");
    }

    @Test
    void read_throwsWhenNoBookRecords() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "books.txt", "text/plain", "# only comment\n".getBytes());

        assertThatThrownBy(() -> bookFileReader.read(file))
                .isInstanceOf(InvalidBookFileException.class)
                .hasMessageContaining("no book records");
    }
}
