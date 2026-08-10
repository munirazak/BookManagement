package com.kopibru.bookmanagement.repository;

import com.kopibru.bookmanagement.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {
}
