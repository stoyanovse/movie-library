package org.alphatrack.movielibrary.services.contracts;

import org.alphatrack.movielibrary.dtos.MovieRequestDto;
import org.alphatrack.movielibrary.dtos.MovieUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.models.User;

import java.util.List;

public interface MovieService {

    List<Movie> getAll(MovieFilterOptions movieFilterOptions);

    Movie getById(Long id);

    Movie update(Long id, MovieUpdateDto movieUpdateDto, User currentUser);

    Movie create(MovieRequestDto movieRequestDto, User currentUser);

    void delete(MovieRequestDto movieRequestDto, User currentUser);

}
