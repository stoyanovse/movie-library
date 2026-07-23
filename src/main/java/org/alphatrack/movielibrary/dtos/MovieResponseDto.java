package org.alphatrack.movielibrary.dtos;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {

    private Long id;

    private String title;

    private String director;

    private Integer releaseYear;

    private Double rating;
}
