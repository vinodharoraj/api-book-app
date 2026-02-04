package au.com.learning.validator;

import au.com.learning.entity.Author;
import au.com.learning.repository.author.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorValidator {

    @Autowired
    private AuthorRepository authorRepository;

    public void validateForUpdate(Long authorId, Author authorRequest,Author existingAuthor) {
        if (authorRequest.getEmail() != null && !authorRequest.getEmail().equals(existingAuthor.getEmail())) {
            if (authorRepository.existsByEmailAndIdNot(authorRequest.getEmail(), authorId)) {
                log.info("Author email {} is already in use", authorRequest.getEmail());
                throw new IllegalArgumentException("Email already in use by another author.");
            }
        }

        if(StringUtils.isBlank(authorRequest.getFirstName()) && StringUtils.isBlank(authorRequest.getLastName())) {
            log.info("At least one of first name or last name must be provided for update.");
            throw new IllegalArgumentException("First name or last name must be provided.");
        }

    }
}

