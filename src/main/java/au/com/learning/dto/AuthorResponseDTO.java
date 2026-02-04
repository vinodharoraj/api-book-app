package au.com.learning.dto;


import java.util.List;

public record AuthorResponseDTO (
        Long id,
        String firstName,
        String lastName,
        String email,
        String bio,
        String genere,
        List<BookResponseDTO> books
) {}

