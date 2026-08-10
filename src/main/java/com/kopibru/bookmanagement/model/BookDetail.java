package com.kopibru.bookmanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "book_detail")
public class BookDetail {

    @Id
    @NotBlank(message = "isbn is required")
    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @NotBlank(message = "title is required")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "author is required")
    @Column(name = "author", nullable = false, length = 255)
    private String author;

    public BookDetail() {
    }

    public BookDetail(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
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
}
