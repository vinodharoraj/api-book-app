package au.com.learning.repository.book.specification;

import au.com.learning.entity.Book;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {

    public static Specification<Book> hasAuthor(String authorName) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(authorName)) {
                return null;
            }
            return cb.or(
                cb.like(cb.lower(root.get("author").get("firstName")), "%" + authorName.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("author").get("lastName")), "%" + authorName.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Book> hasGenre(String genre) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(genre)) {
                return null;
            }
            return cb.equal(cb.lower(root.get("genere")), genre.toLowerCase());
        };
    }
}