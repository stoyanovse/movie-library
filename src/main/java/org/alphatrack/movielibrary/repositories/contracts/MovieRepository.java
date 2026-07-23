package org.alphatrack.movielibrary.repositories.contracts;


import org.alphatrack.movielibrary.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long>,MovieRepositoryCustom {

    boolean existsMovieByDirectorAndTitle(String director, String title);
}
