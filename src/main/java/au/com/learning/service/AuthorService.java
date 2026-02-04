package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.entity.Book;
import au.com.learning.repository.author.AuthorRepository;
import au.com.learning.validator.AuthorValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthorService implements AuthorServiceIF {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorValidator authorValidator;

    @Transactional(readOnly = true)
    public List<AuthorResponseDTO> getAuthorsByName(String name) {

        List<Author> authors = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);

        if (ObjectUtils.isEmpty(authors)) {
            throw new IllegalArgumentException("Author not found");
        }

        return authors.stream().map(this::mapToAuthorResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public AuthorResponseDTO updateAuthor(Long id, Author requestAuthor) {
        log.info("Updating author with id {}", id);
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        authorValidator.validateForUpdate(id, requestAuthor, existingAuthor);

        Optional.ofNullable(requestAuthor.getFirstName()).ifPresent(existingAuthor::setFirstName);
        Optional.ofNullable(requestAuthor.getLastName()).ifPresent(existingAuthor::setLastName);
        Optional.ofNullable(requestAuthor.getEmail()).ifPresent(existingAuthor::setEmail);
        Optional.ofNullable(requestAuthor.getBio()).ifPresent(existingAuthor::setBio);
        Optional.ofNullable(requestAuthor.getGenere()).ifPresent(existingAuthor::setGenere);

        Author updatedAuthor = authorRepository.save(existingAuthor);
        log.info("Updated author with id {}", updatedAuthor.getId());
        return mapToAuthorResponseDTO(updatedAuthor);
    }

    private AuthorResponseDTO mapToAuthorResponseDTO(Author author) {
        List<BookResponseDTO> books = author.getBooks()
                .stream()
                .map(this::mapToBookDTO)
                .collect(Collectors.toList());

        return new AuthorResponseDTO(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                author.getBio(),
                author.getGenere(),
                books
        );
    }

    private BookResponseDTO mapToBookDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getGenere(),
                null
        );
    }
}

