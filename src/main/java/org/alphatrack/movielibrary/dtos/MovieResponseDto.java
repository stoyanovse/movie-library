package org.alphatrack.movielibrary.dtos;

import lombok.*;

@Setter
@Getter
@Builder
public class MovieResponseDto {

    private Long id;

    private String title;

    private String director;

    private Integer releaseYear;
}
