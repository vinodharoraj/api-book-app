package au.com.learning.validator;

import au.com.learning.entity.Author;
import au.com.learning.repository.author.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorValidatorTest {

    private AuthorRepository authorRepository;
    private AuthorValidator authorValidator;

    @BeforeEach
    void setup() {
        authorRepository = mock(AuthorRepository.class);
        authorValidator = new AuthorValidator();
        // Inject mock via reflection since field is \@Autowired
        try {
            var field = AuthorValidator.class.getDeclaredField("authorRepository");
            field.setAccessible(true);
            field.set(authorValidator, authorRepository);
        } catch (Exception e) {
            fail("Failed to inject mock AuthorRepository");
        }
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateForUpdate_throwsWhenEmailAlreadyInUse() {
        Long id = 1L;
        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("old@test.com");

        Author req = new Author();
        req.setEmail("dup@test.com"); // changed email
        req.setFirstName("A");        // valid name

        when(authorRepository.existsByEmailAndIdNot("dup@test.com", id)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorValidator.validateForUpdate(id, req, existing));

        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(authorRepository, times(1)).existsByEmailAndIdNot("dup@test.com", id);
    }

    @Test
    void validateForUpdate_doesNotThrowWhenEmailUnchangedOrNull() {
        Long id = 2L;
        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("same@test.com");

        Author unchanged = new Author();
        unchanged.setEmail("same@test.com");
        unchanged.setFirstName("A");

        Author nullEmail = new Author();
        nullEmail.setEmail(null);
        nullEmail.setFirstName("B");

        assertDoesNotThrow(() -> authorValidator.validateForUpdate(id, unchanged, existing));
        assertDoesNotThrow(() -> authorValidator.validateForUpdate(id, nullEmail, existing));
        verify(authorRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
    }

    @Test
    void validateForUpdate_throwsWhenBothNamesBlank() {
        Long id = 3L;
        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("old@test.com");

        Author req = new Author();
        req.setEmail("old@test.com"); // unchanged, no repository call
        req.setFirstName("   ");      // blank
        req.setLastName(null);        // null

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authorValidator.validateForUpdate(id, req, existing));

        assertTrue(ex.getMessage().toLowerCase().contains("first name or last name"));
        verify(authorRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
    }

    @Test
    void validateForUpdate_doesNotThrowWhenAtLeastOneNameProvided() {
        Long id = 4L;
        Author existing = new Author();
        existing.setId(id);
        existing.setEmail("old@test.com");

        Author req1 = new Author();
        req1.setEmail("old@test.com");
        req1.setFirstName("John"); // first provided
        req1.setLastName("   ");   // blank last

        Author req2 = new Author();
        req2.setEmail("old@test.com");
        req2.setFirstName("   ");  // blank first
        req2.setLastName("Doe");   // last provided

        assertDoesNotThrow(() -> authorValidator.validateForUpdate(id, req1, existing));
        assertDoesNotThrow(() -> authorValidator.validateForUpdate(id, req2, existing));
        verify(authorRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
    }
}

