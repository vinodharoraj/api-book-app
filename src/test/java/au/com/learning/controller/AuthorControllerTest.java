package au.com.learning.controller;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    @Test
    void searchAuthors_returnsListFromService() {
        String name = "john";
        List<AuthorResponseDTO> expected = List.of(
                new AuthorResponseDTO(1L, "John", "Doe", "john@ex.com", null, null, List.of()),
                new AuthorResponseDTO(2L, "Johnny", "Smith", "js@ex.com", null, null, List.of())
        );

        when(authorService.getAuthorsByName(name)).thenReturn(expected);

        List<AuthorResponseDTO> actual = authorController.searchAuthors(name);

        assertEquals(expected, actual);
        verify(authorService, times(1)).getAuthorsByName(name);
        verifyNoMoreInteractions(authorService);
    }

    @Test
    void updateAuthor_returnsDtoFromService() {
        Long id = 10L;

        Author authorDetails = new Author();
        authorDetails.setFirstName("New");
        authorDetails.setLastName("Name");
        authorDetails.setEmail("new@test.com");
        authorDetails.setBio("Bio");
        authorDetails.setGenere("Genre");

        AuthorResponseDTO expected = new AuthorResponseDTO(
                id,
                "New",
                "Name",
                "new@test.com",
                "Bio",
                "Genre",
                List.of()
        );

        when(authorService.updateAuthor(id, authorDetails)).thenReturn(expected);

        AuthorResponseDTO actual = authorController.updateAuthor(id, authorDetails);

        assertEquals(expected, actual);
        verify(authorService, times(1)).updateAuthor(id, authorDetails);
        verifyNoMoreInteractions(authorService);
    }
}

