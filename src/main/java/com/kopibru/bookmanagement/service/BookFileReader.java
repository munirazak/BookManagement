package com.kopibru.bookmanagement.service;

import com.kopibru.bookmanagement.dto.BookDto;
import com.kopibru.bookmanagement.exception.InvalidBookFileException;
import com.kopibru.bookmanagement.model.BookStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class BookFileReader {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt", ".dat");
    private static final String DELIMITER = "\\|";

    public List<BookDto> read(MultipartFile file) {
        validateFile(file);

        List<BookDto> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (isHeader(trimmed)) {
                    continue;
                }
                books.add(parseLine(trimmed, lineNumber));
            }
        } catch (InvalidBookFileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidBookFileException("Failed to read book file", ex);
        }

        if (books.isEmpty()) {
            throw new InvalidBookFileException("Book file contains no book records");
        }
        return books;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidBookFileException("Book file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new InvalidBookFileException("Book file name is required");
        }
        String lowerName = filename.toLowerCase(Locale.ROOT);
        boolean allowed = ALLOWED_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!allowed) {
            throw new InvalidBookFileException("Only .txt or .dat book files are supported");
        }
    }

    private boolean isHeader(String line) {
        String normalized = line.toLowerCase(Locale.ROOT).replace(" ", "");
        return normalized.equals("id|isbn|title|author");
    }

    private BookDto parseLine(String line, int lineNumber) {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != 4) {
            throw new InvalidBookFileException(
                    "Invalid book record at line " + lineNumber
                            + ": expected id|isbn|title|author");
        }

        String id = parts[0].trim();
        String isbn = parts[1].trim();
        String title = parts[2].trim();
        String author = parts[3].trim();

        if (id.isEmpty() || isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
            throw new InvalidBookFileException(
                    "Invalid book record at line " + lineNumber
                            + ": id, isbn, title, and author are required");
        }

        return new BookDto(id, isbn, title, author, BookStatus.AVAILABLE, null);
    }
}
