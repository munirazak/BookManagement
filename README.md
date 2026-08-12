# BookManagement

Spring Boot service that owns the book catalog (`book_detail`, `book_copy`).

**Maven:** `com.kopibru:BookManagement`  
**Port:** `8081`  
**Database:** `book_catalogue`

IndexShelf calls these APIs over HTTP for list / borrow / return (it no longer accesses `book_copy` directly).

## Run

```bash
mvn spring-boot:run
```

## APIs

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/books` | Register book copy |
| `POST` | `/api/books/from-file` | Import from `.txt` / `.dat` |
| `GET` | `/api/books?status=all\|available\|borrowed` | List books |
| `POST` | `/api/books/{bookId}/borrow` | Borrow (`{"libraryId":"..."}`) |
| `POST` | `/api/books/{bookId}/return` | Return (`{"libraryId":"..."}`) |

### Register book
```bash
curl -X POST http://localhost:8081/api/books \
  -H "Content-Type: application/json" \
  -d '{"id":"BOOK-001","isbn":"978-0134685991","title":"Effective Java","author":"Joshua Bloch"}'
```

### Import from file
```bash
curl -X POST http://localhost:8081/api/books/from-file \
  -F "file=@src/main/resources/sample-books.txt"
```

### Borrow / return (used by IndexShelf)
```bash
curl -X POST http://localhost:8081/api/books/BOOK-001/borrow \
  -H "Content-Type: application/json" \
  -d '{"libraryId":"LIB-001"}'
```
