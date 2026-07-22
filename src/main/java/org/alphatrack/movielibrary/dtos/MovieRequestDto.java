package org.alphatrack.movielibrary.dtos;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.*;
import lombok.*;
import org.alphatrack.movielibrary.models.User;

@Setter
@Getter
@Builder
public class MovieRequestDto {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title length should be between 5 and 255 symbols.")
    private String title;

    @NotBlank(message = "Director is required")
    @Size(min = 5, max = 255, message = "Director Name should be between 5 and 255 symbols.")
    private String director;

    @NotNull(message = "Release year is required")
    @Min(value = 1888, message = "Release year cannot be earlier than 1888")
    @Max(value = 2030, message = "Release year is too far in the future")
    private Integer releaseYear;

}
