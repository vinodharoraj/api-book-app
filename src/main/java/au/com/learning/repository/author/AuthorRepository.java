package au.com.learning.repository.author;

import au.com.learning.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByEmail(String email);

    Optional<Author> findByEmail(String email);

    List<Author> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    boolean existsByEmailAndIdNot(String email, Long id);
}

