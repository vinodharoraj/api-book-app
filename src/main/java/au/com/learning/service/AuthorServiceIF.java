package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.entity.Author;

import java.util.List;

public interface AuthorServiceIF {
    List<AuthorResponseDTO> getAuthorsByName(String name);

    AuthorResponseDTO updateAuthor(Long id, Author authorDetails);
}
