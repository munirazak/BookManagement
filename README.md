# BookManagement

Spring Boot service for book catalog registration (register book copy, import from file).

**Maven:** `com.kopibru:BookManagement`  
**Port:** `8081`  
**Database:** shares `book_catalogue` with IndexShelf (`book_detail`, `book_copy`)

## Run

```bash
mvn spring-boot:run
```

## APIs

### Register book
`POST http://localhost:8081/api/books`

```json
{
  "id": "BOOK-001",
  "isbn": "978-0134685991",
  "title": "Effective Java",
  "author": "Joshua Bloch"
}
```

### Import from file
`POST http://localhost:8081/api/books/from-file`  
multipart field `file` (`.txt` / `.dat`, format `id|isbn|title|author`)

```bash
curl -X POST http://localhost:8081/api/books/from-file \
  -F "file=@src/main/resources/sample-books.txt"
```

Borrow / return / list remain on IndexShelf (`http://localhost:8080`).
