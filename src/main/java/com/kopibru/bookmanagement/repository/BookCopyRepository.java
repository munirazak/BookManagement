package com.kopibru.bookmanagement.repository;

import com.kopibru.bookmanagement.model.BookCopy;
import com.kopibru.bookmanagement.model.BookStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail")
    List<BookCopy> findAllWithDetail();

    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail WHERE c.status = :status")
    List<BookCopy> findAllWithDetailByStatus(@Param("status") BookStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT c FROM BookCopy c JOIN FETCH c.bookDetail WHERE c.id = :id")
    Optional<BookCopy> findByIdWithDetailForUpdate(@Param("id") String id);
}
