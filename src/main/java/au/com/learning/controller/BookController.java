package au.com.learning.controller;


import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Book;
import au.com.learning.service.BookServiceIF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class BookController {

    @Autowired
    private BookServiceIF bookService;

    @GetMapping("/getBook/{id}")
    public BookResponseDTO getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/getAllBooks")
    public List<BookResponseDTO> getAllBooks() {
        return  bookService.getAllBooks();
    }

    @GetMapping("/filterBooks")
    public Map<String, List<BookResponseDTO>> filterBooks(@RequestParam(required = true) String author, @RequestParam(required = true) String genre) {
        return bookService.getFilteredBooks(author,genre);
    }

    @PostMapping("/addBook")
    public AuthorResponseDTO addBook(@RequestBody Book book) {
        return bookService.saveBook(book);
    }
}

