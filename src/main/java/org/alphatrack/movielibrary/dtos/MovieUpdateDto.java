package org.alphatrack.movielibrary.dtos;


import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class MovieUpdateDto {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title length should be betwwen 5 and 255 symbols")
    private String title;

    @NotBlank
    @Size(min = 5, max = 255, message = "Director name should be between 5 and 255 symbols")
    private String director;

    @NotNull
    @Min(value = 5, message = "Release year cannot be earlier than 1888")
    @Max(value = 2030, message = "Release year is too far in the future")
    private Integer releaseYear;
}
