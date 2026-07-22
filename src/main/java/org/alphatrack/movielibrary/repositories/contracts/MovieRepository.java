package org.alphatrack.movielibrary.repositories.contracts;

import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long>,MovieRepositoryCustom {

    Optional<Movie> findByTitle(String title);

    boolean existsMovieByTitle(String title);

    boolean existsMovieByDirectorAndTitle(String director, String title);
}
