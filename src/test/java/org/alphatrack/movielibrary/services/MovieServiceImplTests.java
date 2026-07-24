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
import org.alphatrack.movielibrary.utils.mappers.MovieMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MovieServiceImplTests {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private OmdbIntegrationService omdbIntegrationService;

    @InjectMocks
    private MovieServiceImpl movieService;

    private MovieFilterOptions movieFilterOptions;
    private User adminUser;
    private User regularUser;
    private User blockedUser;

    @BeforeEach
    public void init() {
        movieFilterOptions = Mockito.mock(MovieFilterOptions.class);

        adminUser = new User(1L, "admin", "Admin", "User", "pass", Role.ADMIN, "admin@gmail.com", false, true);
        regularUser = new User(2L, "user", "Standard", "User", "pass", Role.USER, "user@gmail.com", false, true);
        blockedUser = new User(3L, "blocked", "Blocked", "User", "pass", Role.USER, "blocked@gmail.com", true, true);
    }

    @AfterEach
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void getAll_Should_ReturnListOfMovies() {
        Movie movie = new Movie();
        movie.setTitle("Inception");

        Mockito.when(movieRepository.findAll(movieFilterOptions))
                .thenReturn(List.of(movie));

        List<Movie> result = movieService.getAll(movieFilterOptions);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Inception", result.get(0).getTitle());
    }

    @Test
    public void getById_Should_Throw_When_UserIsBlocked() {
        Assertions.assertThrows(AccessDeniedException.class, () -> movieService.getById(1L, blockedUser));
        Mockito.verify(movieRepository, Mockito.never()).findById(Mockito.any());
    }

    @Test
    public void getById_Should_Throw_When_MovieNotFound() {
        Mockito.when(movieRepository.findById(1L))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> movieService.getById(1L, regularUser));
    }

    @Test
    public void getById_Should_ReturnMovie_When_Found() {
        Movie mockMovie = Mockito.mock(Movie.class);
        Mockito.when(mockMovie.getId()).thenReturn(1L);

        Mockito.when(movieRepository.findById(1L))
                .thenReturn(Optional.of(mockMovie));

        Movie result = movieService.getById(1L, regularUser);

        Assertions.assertEquals(1L, result.getId());
    }

    @Test
    public void update_Should_Throw_When_MovieNotFound() {
        MovieUpdateDto updateDto = new MovieUpdateDto();
        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> movieService.update(1L, updateDto, adminUser));
    }

    @Test
    public void update_Should_Throw_When_UserIsNotAdmin() {
        Movie movie = new Movie();
        MovieUpdateDto updateDto = new MovieUpdateDto();

        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        Assertions.assertThrows(AccessDeniedException.class, () -> movieService.update(1L, updateDto, regularUser));
    }

    @Test
    public void update_Should_UpdateAndSave_When_Valid() {
        Movie currentMovie = new Movie();

        MovieUpdateDto updateDto = new MovieUpdateDto();
        updateDto.setTitle("New Title");
        updateDto.setDirector("New Director");
        updateDto.setReleaseYear(2025);

        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(currentMovie));

        movieService.update(1L, updateDto, adminUser);

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        Mockito.verify(movieRepository, Mockito.times(1)).save(captor.capture());

        Movie result = captor.getValue();
        Assertions.assertEquals("New Title", result.getTitle());
        Assertions.assertEquals("New Director", result.getDirector());
        Assertions.assertEquals(2025, result.getReleaseYear());
    }

    @Test
    public void create_Should_Throw_When_UserIsNotAdmin() {
        MovieRequestDto requestDto = new MovieRequestDto();
        Assertions.assertThrows(AccessDeniedException.class, () -> movieService.create(requestDto, regularUser));
    }

    @Test
    public void create_Should_Throw_When_MovieAlreadyExists() {
        MovieRequestDto requestDto = new MovieRequestDto();
        requestDto.setTitle("Matrix");
        requestDto.setDirector("Wachowski");

        Mockito.when(movieRepository.existsMovieByDirectorAndTitle("Wachowski", "Matrix"))
                .thenReturn(true);

        Assertions.assertThrows(EntityExistsException.class, () -> movieService.create(requestDto, adminUser));
    }

    @Test
    public void create_Should_MapSaveAndRegisterSync_When_Valid() {
        TransactionSynchronizationManager.initSynchronization();

        MovieRequestDto requestDto = new MovieRequestDto();
        requestDto.setTitle("Matrix");
        requestDto.setDirector("Wachowski");

        Movie mappedMovie = Mockito.mock(Movie.class);
        Mockito.when(mappedMovie.getDirector())
                .thenReturn("Wachowski");


        Mockito.when(movieRepository.existsMovieByDirectorAndTitle("Wachowski", "Matrix")).thenReturn(false);
        Mockito.when(movieMapper.dtoToMovie(requestDto)).thenReturn(mappedMovie);
        Mockito.when(movieRepository.save(mappedMovie)).thenReturn(mappedMovie);

        movieService.create(requestDto, adminUser);

        ArgumentCaptor<Movie> argumentCaptor = ArgumentCaptor.forClass(Movie.class);
        Mockito.verify(movieRepository,Mockito.times(1)).save(argumentCaptor.capture());

        Movie result = argumentCaptor.getValue();

        Assertions.assertEquals("Wachowski", result.getDirector());
    }

    @Test
    public void delete_Should_Throw_When_MovieNotFound() {
        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(EntityNotFoundException.class, () -> movieService.delete(1L, adminUser));
    }

    @Test
    public void delete_Should_Throw_When_UserIsNotAdmin() {
        Movie movie = new Movie();
        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        Assertions.assertThrows(AccessDeniedException.class, () -> movieService.delete(1L, regularUser));
    }

    @Test
    public void delete_Should_CallDelete_When_Valid() {
        Movie movie = new Movie();
        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieService.delete(1L, adminUser);

        Mockito.verify(movieRepository, Mockito.times(1)).delete(movie);
    }
}