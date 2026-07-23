package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.alphatrack.movielibrary.dtos.MovieRequestDto;
import org.alphatrack.movielibrary.dtos.MovieUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepository;
import org.alphatrack.movielibrary.services.contracts.MovieService;
import org.alphatrack.movielibrary.utils.mappers.MovieMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final OmdbIntegrationService omdbIntegrationService;
    private final MovieMapper movieMapper;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository, OmdbIntegrationService omdbIntegrationService,MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.omdbIntegrationService = omdbIntegrationService;
        this.movieMapper = movieMapper;
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

    @Transactional
    @Override
    public Movie update(Long id, MovieUpdateDto movieUpdateDto, User currentUser) {
        Movie currentMovie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d not found", id)));

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException(String.format("You are not authorized to update a movie with id %d", id));
        }

        currentMovie.setReleaseYear(movieUpdateDto.getReleaseYear());
        currentMovie.setDirector(movieUpdateDto.getDirector());
        currentMovie.setTitle(movieUpdateDto.getTitle());

        return movieRepository.save(currentMovie);
    }

    @Transactional
    @Override
    public Movie create(MovieRequestDto movieRequestDto, User currentUser) {

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException("You are not authorized to add movies in the library");
        }

        if (movieRepository.existsMovieByDirectorAndTitle(movieRequestDto.getDirector(), movieRequestDto.getTitle())) {
            throw new EntityExistsException(
                    String.format("Movie with title %s and director %s already exists", movieRequestDto.getTitle(), movieRequestDto.getDirector()));
        }

           Movie movie = movieMapper.dtoToMovie(movieRequestDto);
           Movie savedMovie = movieRepository.save(movie);
           omdbIntegrationService.fetchAndSaveRating(savedMovie.getId(), savedMovie.getTitle());

           return savedMovie;
    }

    @Transactional
    @Override
    public void delete(Long id, User currentUser) {
        Movie movieToDelete = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d not found", id)));

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException(String.format("You are not authorized to delete a movie with id %d", id));
        }


        movieRepository.delete(movieToDelete);
    }

    private boolean isAdmin(User currentUser) {
        return currentUser.getRole().equals(Role.ADMIN);
    }
}
