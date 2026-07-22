package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.alphatrack.movielibrary.dtos.MovieRequestDto;
import org.alphatrack.movielibrary.dtos.MovieUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepository;
import org.alphatrack.movielibrary.services.contracts.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private MovieRepository movieRepository;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public List<Movie> getAll(MovieFilterOptions movieFilterOptions) {
        return movieRepository.findAll(movieFilterOptions);
    }

    @Override
    public Movie getById(Long id, User currentUser) {
        if (currentUser.getIsBlocked()) {
            throw new AccessDeniedException("You are currently blocked and have restricted access");
        }

        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d not found", id)));
    }

    @Override
    public Movie update(Long id, MovieUpdateDto movieUpdateDto, User currentUser) {
        Movie currentMovie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d not found", id)));

        boolean isOwner = currentMovie.getAddedBy().getUsername().equals(currentUser.getUsername());
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(String.format("You are not authorized to update a movie with id %d", id));
        }

        currentMovie.setReleaseYear(movieUpdateDto.getReleaseYear());
        currentMovie.setDirector(movieUpdateDto.getDirector());
        currentMovie.setTitle(movieUpdateDto.getTitle());

        return movieRepository.save(currentMovie);
    }

    @Override
    public Movie create(MovieRequestDto movieRequestDto, User currentUser) {

        if (movieRepository.existsMovieByDirectorAndTitle(movieRequestDto.getDirector(), movieRequestDto.getTitle())) {
            throw new EntityExistsException(
                    String.format("Movie with title %s and director %s already exists", movieRequestDto.getTitle(), movieRequestDto.getDirector()));
        }

           Movie movie = Movie.builder()
                   .title(movieRequestDto.getTitle())
                   .director(movieRequestDto.getDirector())
                   .releaseYear(movieRequestDto.getReleaseYear())
                   .addedBy(currentUser)
                   .rating(null)
                   .build();

           return movieRepository.save(movie);
    }

    @Override
    public void delete(Long id, User currentUser) {
        Movie movieToDelete = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d not found", id)));

        boolean isOwner = movieToDelete.getAddedBy().getUsername().equals(currentUser.getUsername());
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(String.format("You are not authorized to delete a movie with id %d", id));
        }

        movieToDelete.getAddedBy().getMovies().remove(movieToDelete);
        movieRepository.delete(movieToDelete);
    }
}
