package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.entity.Book;
import au.com.learning.repository.author.AuthorRepository;
import au.com.learning.repository.book.BookRepository;
import au.com.learning.validator.AuthorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorValidator authorValidator;

    @InjectMocks
    private AuthorService authorService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAuthorsByName_returnsMappedDTOs() {
        Author a1 = new Author();
        a1.setId(1L);
        a1.setFirstName("Jane");
        a1.setLastName("Austen");
        a1.setEmail("jane@test.com");
        a1.setBio("Bio");
        a1.setGenere("Classic");

        Book b1 = new Book();
        b1.setId(10L);
        b1.setTitle("Pride and Prejudice");
        b1.setGenere("Novel");
        b1.setAuthor(a1);
        a1.setBooks(Collections.singletonList(b1));

        when(authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jan", "jan"))
                .thenReturn(List.of(a1));

        List<AuthorResponseDTO> result = authorService.getAuthorsByName("jan");

        assertNotNull(result);
        assertEquals(1, result.size());
        AuthorResponseDTO dto = result.get(0);
        assertEquals(1L, dto.id());
        assertEquals("Jane", dto.firstName());
        assertEquals("Austen", dto.lastName());
        assertEquals("jane@test.com", dto.email());
        assertEquals("Bio", dto.bio());
        assertEquals("Classic", dto.genere());
        assertNotNull(dto.books());
        assertEquals(1, dto.books().size());
        assertEquals(10L, dto.books().get(0).id());
        assertEquals("Pride and Prejudice", dto.books().get(0).title());
        assertEquals("Novel", dto.books().get(0).genere());

        verify(authorRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jan", "jan");
        verifyNoMoreInteractions(authorRepository, bookRepository);
    }

    @Test
    void getAuthorsByName_emptyResult_returnsEmptyList() {
        when(authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("x", "x"))
                .thenReturn(Collections.emptyList());


        RuntimeException ex = assertThrows(RuntimeException.class, () -> authorService.getAuthorsByName("x"));
        assertTrue(ex.getMessage().toLowerCase().contains("author"));
        verify(authorRepository, times(1))
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("x", "x");
        verifyNoMoreInteractions(authorRepository, bookRepository);
    }

    @Test
    void updateAuthor_updatesAndReturnsDTO() {
        long id = 2L;
        Author existing = new Author();
        existing.setId(id);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setEmail("old@test.com");
        existing.setBio("Old bio");
        existing.setGenere("Old genere");
        existing.setBooks(Collections.emptyList());

        Author update = new Author();
        update.setFirstName("New");
        update.setLastName("Name");
        update.setEmail("new@test.com");
        update.setBio("New bio");
        update.setGenere("New genere");
        update.setBooks(Collections.emptyList());

        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorRepository.existsByEmailAndIdNot("new@test.com", id)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthorResponseDTO dto = authorService.updateAuthor(id, update);

        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("New", dto.firstName());
        assertEquals("Name", dto.lastName());
        assertEquals("new@test.com", dto.email());
        assertEquals("New bio", dto.bio());
        assertEquals("New genere", dto.genere());
        assertNotNull(dto.books());
        assertTrue(dto.books().isEmpty());

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        Author saved = captor.getValue();
        assertEquals("New", saved.getFirstName());
        assertEquals("Name", saved.getLastName());
        assertEquals("new@test.com", saved.getEmail());
    }

    @Test
    void updateAuthor_throwsWhenEmailAlreadyInUse() {
        long id = 3L;

        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("old@test.com");
        existing.setFirstName("Old");
        existing.setLastName("Name");

        Author update = new Author();
        update.setFirstName("A");
        update.setLastName("B");
        update.setEmail("dup@test.com");

        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorRepository.existsByEmailAndIdNot("dup@test.com", id)).thenReturn(true);


        doThrow(new IllegalArgumentException("Author email is already in use"))
                .when(authorValidator).validateForUpdate(eq(id), eq(update), eq(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorService.updateAuthor(id, update));

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(authorRepository, times(1)).findById(id);
        verify(authorValidator, times(1)).validateForUpdate(id, update, existing);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_throwsWhenAuthorNotFound() {
        long id = 999L;
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorService.updateAuthor(id, new Author()));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(authorRepository, times(1)).findById(id);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_throwsWhenInvalidNamesOrEmail() {
        long id = 4L;
        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("old@test.com");
        existing.setFirstName("Old");
        existing.setLastName("Name");

        Author badUpdate = new Author();
        badUpdate.setFirstName("  ");
        badUpdate.setLastName(null);
        badUpdate.setEmail("  ");

        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));


        doThrow(new IllegalArgumentException("Invalid author input"))
                .when(authorValidator).validateForUpdate(eq(id), eq(badUpdate), eq(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorService.updateAuthor(id, badUpdate));

        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
        verify(authorRepository, times(1)).findById(id);
        verify(authorValidator, times(1)).validateForUpdate(id, badUpdate, existing);
        verify(authorRepository, never()).save(any());
    }
}

