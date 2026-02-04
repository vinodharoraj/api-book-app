package au.com.learning.controller;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Book;
import au.com.learning.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    void getAllBooks_returnsListFromService() {
        List<BookResponseDTO> expected = List.of(
                mock(BookResponseDTO.class),
                mock(BookResponseDTO.class)
        );

        when(bookService.getAllBooks()).thenReturn(expected);

        List<BookResponseDTO> actual = bookController.getAllBooks();

        assertEquals(expected, actual);
        verify(bookService, times(1)).getAllBooks();
        verifyNoMoreInteractions(bookService);
    }

    @Test
    void addBook_returnsAuthorResponseFromService() {
        Book book = new Book();
        AuthorResponseDTO expected = mock(AuthorResponseDTO.class);

        when(bookService.saveBook(book)).thenReturn(expected);

        AuthorResponseDTO actual = bookController.addBook(book);

        assertEquals(expected, actual);
        verify(bookService, times(1)).saveBook(book);
        verifyNoMoreInteractions(bookService);
    }

    @Test
    void filterBooks_returnsGroupedMapFromService() {
        BookResponseDTO dto1 = mock(BookResponseDTO.class);
        BookResponseDTO dto2 = mock(BookResponseDTO.class);
        Map<String, List<BookResponseDTO>> expected = Map.of("Novel", List.of(dto1, dto2));

        when(bookService.getFilteredBooks("Jane", "Novel")).thenReturn(expected);

        Map<String, List<BookResponseDTO>> actual = bookController.filterBooks("Jane", "Novel");

        assertEquals(expected, actual);
        verify(bookService, times(1)).getFilteredBooks("Jane", "Novel");
        verifyNoMoreInteractions(bookService);
    }

    @Test
    void getBookById_returnsDtoFromService() {
        BookResponseDTO expected = mock(BookResponseDTO.class);
        when(bookService.getBookById(100L)).thenReturn(expected);

        BookResponseDTO actual = bookController.getBookById(100L);

        assertSame(expected, actual);
        verify(bookService, times(1)).getBookById(100L);
        verifyNoMoreInteractions(bookService);
    }
}

