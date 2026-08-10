package com.kopibru.bookmanagement.dto;

import com.kopibru.bookmanagement.model.BookStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

public class BookDto {

    @NotBlank(message = "id is required")
    private String id;

    @NotBlank(message = "isbn is required")
    private String isbn;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "author is required")
    private String author;

    private BookStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String libraryId;

    public BookDto() {
    }

    public BookDto(String id, String isbn, String title, String author, BookStatus status, String libraryId) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.status = status;
        this.libraryId = libraryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }
}
