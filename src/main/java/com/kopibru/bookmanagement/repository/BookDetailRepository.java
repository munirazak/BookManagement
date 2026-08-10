package com.kopibru.bookmanagement.repository;

import com.kopibru.bookmanagement.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, String> {
}
