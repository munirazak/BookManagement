package com.kopibru.bookmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowRequest {

    @NotBlank(message = "libraryId is required")
    private String libraryId;

    public BorrowRequest() {
    }

    public BorrowRequest(String libraryId) {
        this.libraryId = libraryId;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }
}
