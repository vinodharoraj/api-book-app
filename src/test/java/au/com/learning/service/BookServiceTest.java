package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.entity.Book;
import au.com.learning.repository.author.AuthorRepository;
import au.com.learning.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Author authorJane;
    private Author authorMark;
    private Book book1;
    private Book book2;
    private Book book3;
    private Author existingAuthor;
    private Book requestBook;

    @BeforeEach
    void setUp() {
        existingAuthor = new Author();
        existingAuthor.setId(1L);
        existingAuthor.setFirstName("John");
        existingAuthor.setLastName("Doe");
        existingAuthor.setEmail("john.doe@test.com");
        existingAuthor.setBooks(Collections.emptyList());

        requestBook = new Book();
        requestBook.setTitle("Spring in Action");
        requestBook.setGenere("Tech");
        requestBook.setAuthor(existingAuthor);

        authorJane = new Author();
        authorJane.setId(5L);
        authorJane.setFirstName("Jane");
        authorJane.setLastName("Austen");
        authorJane.setEmail("jane.austen@test.com");
        authorJane.setBio("Bio");
        authorJane.setGenere("Classic");

        authorMark = new Author();
        authorMark.setId(6L);
        authorMark.setFirstName("Mark");
        authorMark.setLastName("Twain");
        authorMark.setEmail("mark.twain@test.com");
        authorMark.setBio("Bio");
        authorMark.setGenere("Classic");

        book1 = new Book();
        book1.setId(100L);
        book1.setTitle("Pride and Prejudice");
        book1.setGenere("Novel");
        book1.setAuthor(authorJane);

        book2 = new Book();
        book2.setId(101L);
        book2.setTitle("Emma");
        book2.setGenere("Novel");
        book2.setAuthor(authorJane);

        book3 = new Book();
        book3.setId(200L);
        book3.setTitle("Adventures");
        book3.setGenere("Adventure");
        book3.setAuthor(authorMark);
    }

    @Test
    void saveBook_throwsWhenBookIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.saveBook(null));
        verifyNoInteractions(bookRepository, authorRepository);
    }

    @Test
    void saveBook_throwsWhenTitleBlank() {
        requestBook.setTitle("  ");
        assertThrows(IllegalArgumentException.class,
                () -> bookService.saveBook(requestBook));
        verifyNoInteractions(bookRepository, authorRepository);
    }

    @Test
    void saveBook_throwsWhenAuthorNull() {
        requestBook.setAuthor(null);
        assertThrows(IllegalArgumentException.class,
                () -> bookService.saveBook(requestBook));
        verifyNoInteractions(bookRepository, authorRepository);
    }

    @Test
    void saveBook_throwsWhenAuthorEmailBlank() {
        existingAuthor.setEmail(" ");
        assertThrows(IllegalArgumentException.class,
                () -> bookService.saveBook(requestBook));
        verifyNoInteractions(bookRepository, authorRepository);
    }

    @Test
    void saveBook_usesExistingAuthor_whenEmailExists() {
        when(authorRepository.findByEmail(existingAuthor.getEmail()))
                .thenReturn(Optional.of(existingAuthor));
        when(bookRepository.existsByTitleAndAuthorEmail(
                requestBook.getTitle(), existingAuthor.getEmail()))
                .thenReturn(false);
        when(bookRepository.save(requestBook))
                .thenAnswer(invocation -> {
                    Book b = invocation.getArgument(0);
                    b.setId(10L);
                    return b;
                });

        AuthorResponseDTO response = bookService.saveBook(requestBook);

        assertNotNull(response);
        assertEquals(existingAuthor.getId(), response.id());
        verify(authorRepository, times(1))
                .findByEmail(existingAuthor.getEmail());
        verify(authorRepository, never()).save(any());
        verify(bookRepository, times(1)).save(requestBook);
    }

    @Test
    void saveBook_createsNewAuthor_whenEmailNotFound() {
        Author newAuthor = new Author();
        newAuthor.setFirstName("Alice");
        newAuthor.setLastName("Smith");
        newAuthor.setEmail("alice@test.com");

        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setGenere("Fiction");
        newBook.setAuthor(newAuthor);

        when(authorRepository.findByEmail(newAuthor.getEmail()))
                .thenReturn(Optional.empty());
        when(authorRepository.save(newAuthor))
                .thenAnswer(invocation -> {
                    Author a = invocation.getArgument(0);
                    a.setId(2L);
                    return a;
                });
        when(bookRepository.existsByTitleAndAuthorEmail(
                newBook.getTitle(), newAuthor.getEmail()))
                .thenReturn(false);
        when(bookRepository.save(newBook))
                .thenAnswer(invocation -> {
                    Book b = invocation.getArgument(0);
                    b.setId(20L);
                    return b;
                });

        AuthorResponseDTO response = bookService.saveBook(newBook);

        assertNotNull(response);
        assertEquals(2L, response.id());
        verify(authorRepository).save(newAuthor);
        verify(bookRepository).save(newBook);
    }

//    @Test
//    void saveBook_throwsWhenNewAuthorMissingFirstOrLastName() {
//        Author newAuthor = new Author();
//        newAuthor.setFirstName("");
//        newAuthor.setLastName("Smith");
//        newAuthor.setEmail("alice@test.com");
//
//        Book newBook = new Book();
//        newBook.setTitle("New Book");
//        newBook.setGenere("Fiction");
//        newBook.setAuthor(newAuthor);
//
//        when(authorRepository.findByEmail(newAuthor.getEmail()))
//                .thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class,
//                () -> bookService.saveBook(newBook));
//
//        verify(authorRepository, never()).save(any());
//        verify(bookRepository, never()).save(any());
//    }

    @Test
    void saveBook_throwsWhenDuplicateBookExists() {
        when(authorRepository.findByEmail(existingAuthor.getEmail()))
                .thenReturn(Optional.of(existingAuthor));
        when(bookRepository.existsByTitleAndAuthorEmail(
                requestBook.getTitle(), existingAuthor.getEmail()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> bookService.saveBook(requestBook));

        verify(bookRepository, never()).save(any());
    }


    @Test
    void getAllBooks_returnsMappedBookResponseDTOList() {
        // Arrange
        Author author = new Author();
        author.setId(5L);
        author.setFirstName("Jane");
        author.setLastName("Austen");
        author.setEmail("jane.austen@test.com");
        author.setBio("Bio");
        author.setGenere("Classic");

        Book book = new Book();
        book.setId(100L);
        book.setTitle("Pride and Prejudice");
        book.setGenere("Novel");
        book.setAuthor(author);

        when(bookRepository.findAll()).thenReturn(Collections.singletonList(book));

        // Act
        var result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        var bookDto = result.get(0);
        assertEquals(100L, bookDto.id());
        assertEquals("Pride and Prejudice", bookDto.title());
        assertEquals("Novel", bookDto.genere());
        assertNotNull(bookDto.author());

        var authorDto = bookDto.author();
        assertEquals(5L, authorDto.id());
        assertEquals("Jane", authorDto.firstName());
        assertEquals("Austen", authorDto.lastName());
        assertEquals("jane.austen@test.com", authorDto.email());
        assertEquals("Bio", authorDto.bio());
        assertEquals("Classic", authorDto.genere());

        verify(bookRepository, times(1)).findAll();
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getFilteredBooks_noFilters_returnsGroupedByGenre() {
        List<Book> repoResult = Arrays.asList(book1, book3);
        when(bookRepository.findAll(nullable(Specification.class))).thenReturn(repoResult);

        Map<String, List<BookResponseDTO>> result = bookService.getFilteredBooks(null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Novel"));
        assertTrue(result.containsKey("Adventure"));

        List<BookResponseDTO> novels = result.get("Novel");
        assertEquals(1, novels.size());
        BookResponseDTO dto = novels.get(0);
        assertEquals(100L, dto.id());
        assertEquals("Pride and Prejudice", dto.title());
        assertEquals("Novel", dto.genere());
        assertNotNull(dto.author());
        assertEquals(5L, dto.author().id());

        verify(bookRepository, times(1)).findAll(nullable(Specification.class));
    }

    @Test
    void getFilteredBooks_authorFilter_returnsOnlyMatchingBooksGrouped() {
        // Simulate repository returning only books matching author filter
        List<Book> repoResult = Arrays.asList(book1, book2); // both Jane, same genre Novel
        when(bookRepository.findAll(nullable(Specification.class))).thenReturn(repoResult);

        Map<String, List<BookResponseDTO>> result = bookService.getFilteredBooks("Jane", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("Novel"));
        List<BookResponseDTO> novels = result.get("Novel");
        assertEquals(2, novels.size());

        // check mapping of both items
        assertEquals(100L, novels.get(0).id());
        assertEquals(101L, novels.get(1).id());

        verify(bookRepository, times(1)).findAll(nullable(Specification.class));
    }

    @Test
    void getFilteredBooks_authorAndGenreFilter_returnsMatchingGrouping() {
        // Simulate repository returning only the book that matches both filters
        List<Book> repoResult = Arrays.asList(book1);
        when(bookRepository.findAll(nullable(Specification.class))).thenReturn(repoResult);

        Map<String, List<BookResponseDTO>> result = bookService.getFilteredBooks("Jane", "Novel");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("Novel"));
        List<BookResponseDTO> novels = result.get("Novel");
        assertEquals(1, novels.size());
        BookResponseDTO dto = novels.get(0);
        assertEquals(100L, dto.id());
        assertEquals("Pride and Prejudice", dto.title());
        assertEquals("Novel", dto.genere());
        assertNotNull(dto.author());
        assertEquals("Jane", dto.author().firstName());

        verify(bookRepository, times(1)).findAll(nullable(Specification.class));
    }

    @Test
    void getBookById_returnsMappedBookResponseDTO_whenBookExists() {
        // Arrange
        Author author = new Author();
        author.setId(5L);
        author.setFirstName("Jane");
        author.setLastName("Austen");
        author.setEmail("jane.austen@test.com");
        author.setBio("Bio");
        author.setGenere("Classic");

        Book book = new Book();
        book.setId(100L);
        book.setTitle("Pride and Prejudice");
        book.setGenere("Novel");
        book.setAuthor(author);

        when(bookRepository.findById(100L)).thenReturn(Optional.of(book));

        // Act
        BookResponseDTO result = bookService.getBookById(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals("Pride and Prejudice", result.title());
        assertEquals("Novel", result.genere());
        assertNotNull(result.author());
        assertEquals(5L, result.author().id());
        assertEquals("Jane", result.author().firstName());
        assertEquals("Austen", result.author().lastName());
        assertEquals("jane.austen@test.com", result.author().email());

        verify(bookRepository, times(1)).findById(100L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBookById_throwsIllegalArgumentException_whenBookNotFound() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookService.getBookById(999L));
        assertTrue(ex.getMessage().contains("Book not found with ID: 999"));

        verify(bookRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(bookRepository);
    }

}

