-- Book catalog tables. Shares database with IndexShelf (borrowers live there).

CREATE TABLE IF NOT EXISTS book_detail (
    isbn   VARCHAR(20)  PRIMARY KEY,
    title  VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS book_copy (
    id         VARCHAR(100) PRIMARY KEY,
    isbn       VARCHAR(20)  NOT NULL REFERENCES book_detail (isbn),
    status     VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    library_id VARCHAR(100)
);
