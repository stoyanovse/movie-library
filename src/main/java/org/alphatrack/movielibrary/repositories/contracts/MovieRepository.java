package org.alphatrack.movielibrary.repositories.contracts;


import org.alphatrack.movielibrary.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long>,MovieRepositoryCustom {

    boolean existsMovieByDirectorAndTitle(String director, String title);
}
