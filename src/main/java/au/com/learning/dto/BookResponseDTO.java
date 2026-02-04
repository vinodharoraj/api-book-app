package au.com.learning.dto;

public record BookResponseDTO (Long id, String title, String genere,AuthorResponseDTO author) {}

