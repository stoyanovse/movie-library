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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    public static final String MOVIE_WITH_ID_NOT_FOUND = "Movie with id %d not found";
    public static final String CURRENTLY_BLOCKED = "You are currently blocked and have restricted access";
    public static final String NOT_AUTHORIZED_TO_UPDATE_A_MOVIE = "You are not authorized to update a movie with id %d";
    public static final String NOT_AUTHORIZED_TO_ADD_MOVIES_IN_THE_LIBRARY = "You are not authorized to add movies in the library";
    public static final String NOT_AUTHORIZED_TO_DELETE_A_MOVIE = "You are not authorized to delete a movie with id %d";
    public static final String MOVIE_EXISTS = "Movie with title %s and director %s already exists";
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
            throw new AccessDeniedException(CURRENTLY_BLOCKED);
        }

        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MOVIE_WITH_ID_NOT_FOUND, id)));
    }


    @Transactional
    @Override
    public Movie update(Long id, MovieUpdateDto movieUpdateDto, User currentUser) {
        Movie currentMovie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MOVIE_WITH_ID_NOT_FOUND, id)));

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException(String.format(NOT_AUTHORIZED_TO_UPDATE_A_MOVIE, id));
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
            throw new AccessDeniedException(NOT_AUTHORIZED_TO_ADD_MOVIES_IN_THE_LIBRARY);
        }

        if (movieRepository.existsMovieByDirectorAndTitle(movieRequestDto.getDirector(), movieRequestDto.getTitle())) {
            throw new EntityExistsException(
                    String.format(MOVIE_EXISTS, movieRequestDto.getTitle(), movieRequestDto.getDirector()));
        }

           Movie movie = movieMapper.dtoToMovie(movieRequestDto);
           Movie savedMovie = movieRepository.save(movie);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                omdbIntegrationService.fetchAndSaveRating(savedMovie.getId(), savedMovie.getTitle());
            }
        });

           return savedMovie;
    }

    @Transactional
    @Override
    public void delete(Long id, User currentUser) {
        Movie movieToDelete = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MOVIE_WITH_ID_NOT_FOUND, id)));

        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException(String.format(NOT_AUTHORIZED_TO_DELETE_A_MOVIE, id));
        }


        movieRepository.delete(movieToDelete);
    }

    private boolean isAdmin(User currentUser) {
        return currentUser.getRole().equals(Role.ADMIN);
    }
}
