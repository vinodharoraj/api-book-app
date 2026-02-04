package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Book;

import java.util.List;
import java.util.Map;

public interface BookServiceIF {
    List<BookResponseDTO> getAllBooks();

    AuthorResponseDTO saveBook(Book book);

    Map<String, List<BookResponseDTO>> getFilteredBooks(String author, String genre);

    BookResponseDTO getBookById(Long id);
}
