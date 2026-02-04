package au.com.learning.controller;

import au.com.learning.dto.AuthorResponseDTO;
import au.com.learning.entity.Author;
import au.com.learning.service.AuthorServiceIF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    @Autowired
    private AuthorServiceIF authorService;

    @GetMapping("/search")
    public List<AuthorResponseDTO> searchAuthors(@RequestParam String name) {
        return authorService.getAuthorsByName(name);
    }

    @PutMapping("/update/{id}")
    public AuthorResponseDTO updateAuthor(@PathVariable Long id, @RequestBody Author authorDetails) {
        return authorService.updateAuthor(id, authorDetails);
    }
}

