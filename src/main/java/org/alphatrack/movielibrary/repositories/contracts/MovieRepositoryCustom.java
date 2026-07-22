package org.alphatrack.movielibrary.repositories.contracts;

import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;

import java.util.List;

public interface MovieRepositoryCustom {
    List<Movie> findAll(MovieFilterOptions movieFilterOptions);
}
