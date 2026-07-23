package org.alphatrack.movielibrary.utils.mappers;

import org.alphatrack.movielibrary.dtos.MovieRequestDto;
import org.alphatrack.movielibrary.dtos.MovieResponseDto;
import org.alphatrack.movielibrary.models.Movie;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieMapper {

    public Movie dtoToMovie(MovieRequestDto movieRequestDto) {
        return Movie.builder()
                .title(movieRequestDto.getTitle())
                .director(movieRequestDto.getDirector())
                .releaseYear(movieRequestDto.getReleaseYear())
                .rating(null)
                .build();
    }

    public MovieResponseDto movieToDto(Movie movie) {
        return MovieResponseDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .director(movie.getDirector())
                .releaseYear(movie.getReleaseYear())
                .rating(movie.getRating())
                .build();
    }

    public List<MovieResponseDto> movieListToResponseDtoList(List<Movie> movies) {
        return movies.stream()
                .map(this::movieToDto)
                .toList();
    }
}
