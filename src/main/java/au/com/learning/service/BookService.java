package au.com.learning.service;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.dto.BookResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.entity.Book;
import au.com.learning.repository.author.AuthorRepository;
import au.com.learning.repository.book.BookRepository;
import au.com.learning.repository.book.specification.BookSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService implements BookServiceIF{


    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {
         List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(book -> {
                    AuthorResponseDTO authorResponseDTO = new AuthorResponseDTO(
                            book.getAuthor().getId(),
                            book.getAuthor().getFirstName(),
                            book.getAuthor().getLastName(),
                            book.getAuthor().getEmail(),
                            book.getAuthor().getBio(),
                            book.getAuthor().getGenere(),
                            null);
                    return  new BookResponseDTO(book.getId(), book.getTitle(), book.getGenere(), authorResponseDTO);
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuthorResponseDTO saveBook(Book requestBook){

        if(ObjectUtils.isEmpty(requestBook) || StringUtils.isBlank(requestBook.getTitle())){
            log.error("Book request cannot be null");
            throw new IllegalArgumentException("Book request or Book title cannot be empty or null");
        }

        Author requestAuthor = requestBook.getAuthor();
        String requestBookTitle = requestBook.getTitle();

        if(ObjectUtils.isEmpty(requestAuthor)){
            log.error("Book author cannot be null");
            throw new IllegalArgumentException("Book author cannot be null");
        }

        if(StringUtils.isBlank(requestAuthor.getEmail())){
            log.info("Author email is required");
            throw new IllegalArgumentException("Author email is required");
        }

        Author managedAuthor = authorRepository.findByEmail(requestAuthor.getEmail())
                .map(existingAuthor -> {
                    log.info("Author email already exists {}", existingAuthor.getEmail());
                    return existingAuthor;
                }).orElseGet(() -> {
                    if(StringUtils.isBlank(requestAuthor.getFirstName()) && StringUtils.isBlank(requestAuthor.getLastName())){
                        log.error("New author must have first name and last name");
                        throw new IllegalArgumentException("New author must have first name and last name");
                    }
                    return authorRepository.save(requestAuthor);
                });

        if(bookRepository.existsByTitleAndAuthorEmail(requestBookTitle, requestAuthor.getEmail())){
            log.error("Book with title {} by author email {} already exists", requestBookTitle, requestAuthor.getEmail());
            throw new IllegalArgumentException("Book with the same title by the same author already exists");
        }

        log.info("Author updated {} with first name {}", managedAuthor, managedAuthor.getFirstName());
        requestBook.setAuthor(managedAuthor);
        bookRepository.save(requestBook);
        log.info("Book saved successfully with title {}, preparing the response", requestBookTitle);

        return mapToAuthorResponseDTO(requestBook);
    }

    @Override
    public Map<String, List<BookResponseDTO>> getFilteredBooks(String author, String genre) {
        List<BookResponseDTO> filteredBooks = getFilteredBooksByAuthorAndGenre(author, genre);
        return filteredBooks.stream().collect(Collectors.groupingBy(BookResponseDTO::genere));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
        return mapToBookResponseDTO(book);
    }


    @Transactional(readOnly = true)
    public List<BookResponseDTO> getFilteredBooksByAuthorAndGenre(String author, String genre){
        Specification<Book> bookSpecification = Specification.where(null);

        if(!StringUtils.isBlank(author)){
            bookSpecification = bookSpecification.and(BookSpecifications.hasAuthor(author));
        }
        if(!StringUtils.isBlank(genre)){
            bookSpecification = bookSpecification.and(BookSpecifications.hasGenre(genre));
        }

        List<Book> books = bookRepository.findAll(bookSpecification);
        return books.stream().map(this::mapToBookResponseDTO).collect(Collectors.toList());
    }

    private BookResponseDTO mapToBookResponseDTO(Book book) {
        AuthorResponseDTO authorResponseDTO = null;
        if(!ObjectUtils.isEmpty(book.getAuthor())) {
            authorResponseDTO = new AuthorResponseDTO(
                    book.getAuthor().getId(),
                    book.getAuthor().getFirstName(),
                    book.getAuthor().getLastName(),
                    book.getAuthor().getEmail(),
                    book.getAuthor().getBio(),
                    book.getAuthor().getGenere(),
                    null);
        }

        return  new BookResponseDTO(book.getId(), book.getTitle(), book.getGenere(), authorResponseDTO);
        }

    private AuthorResponseDTO mapToAuthorResponseDTO(Book responseBook) {
        BookResponseDTO bookResponseDTO = new BookResponseDTO(
                responseBook.getId(),
                responseBook.getTitle(),
                responseBook.getGenere(),
                null
        );
        Author author = responseBook.getAuthor();
        List<BookResponseDTO> books = new ArrayList<>();
        books.add(bookResponseDTO);
        return new AuthorResponseDTO(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                author.getBio(),
                author.getGenere(),
                books);
    }
}

