package org.alphatrack.movielibrary.services;

import org.alphatrack.movielibrary.dtos.OmdbResponseDto;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class OmdbIntegrationServiceTests {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Mock
    private MovieRepository movieRepository;

    private OmdbIntegrationService omdbIntegrationService;

    @BeforeEach
    public void init() {
        Mockito.when(restClientBuilder.baseUrl(Mockito.anyString())).thenReturn(restClientBuilder);
        Mockito.when(restClientBuilder.build()).thenReturn(restClient);

        omdbIntegrationService = new OmdbIntegrationService(restClientBuilder, movieRepository, "dummy-api-key");
    }

    @Test
    public void fetchAndSaveRating_Should_SaveRating_When_ValidResponseAndMovieExists() {
        OmdbResponseDto mockResponse = Mockito.mock(OmdbResponseDto.class);
        Mockito.when(mockResponse.getResponse()).thenReturn("True");
        Mockito.when(mockResponse.getImdbRating()).thenReturn("8.5");

        Mockito.when(restClient.get()
                        .uri(Mockito.<Function<UriBuilder, URI>>any())
                        .retrieve()
                        .body(OmdbResponseDto.class))
                .thenReturn(mockResponse);

        Movie mockMovie = Mockito.mock(Movie.class);
        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));

        omdbIntegrationService.fetchAndSaveRating(1L, "The Matrix");

        Mockito.verify(mockMovie, Mockito.times(1)).setRating(8.5);
        Mockito.verify(movieRepository, Mockito.times(1)).save(mockMovie);
    }

    @Test
    public void fetchAndSaveRating_Should_NotSave_When_MovieNotFoundInDb() {
        OmdbResponseDto mockResponse = Mockito.mock(OmdbResponseDto.class);
        Mockito.when(mockResponse.getResponse()).thenReturn("True");
        Mockito.when(mockResponse.getImdbRating()).thenReturn("8.5");

        Mockito.when(restClient.get()
                        .uri(Mockito.<Function<UriBuilder,URI>>any())
                        .retrieve()
                        .body(OmdbResponseDto.class))
                .thenReturn(mockResponse);

        Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        omdbIntegrationService.fetchAndSaveRating(1L, "The Matrix");

        Mockito.verify(movieRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void fetchAndSaveRating_Should_NotCallDatabase_When_OmdbResponseIsFalse() {
        OmdbResponseDto mockResponse = Mockito.mock(OmdbResponseDto.class);
        Mockito.when(mockResponse.getResponse()).thenReturn("False");

        Mockito.when(restClient.get()
                        .uri(Mockito.<Function<UriBuilder,URI>>any())
                        .retrieve()
                        .body(OmdbResponseDto.class))
                .thenReturn(mockResponse);

        omdbIntegrationService.fetchAndSaveRating(1L, "The Matrix");

        Mockito.verify(movieRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(movieRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void fetchAndSaveRating_Should_NotCallDatabase_When_RatingIsNA() {
        OmdbResponseDto mockResponse = Mockito.mock(OmdbResponseDto.class);
        Mockito.when(mockResponse.getResponse()).thenReturn("True");
        Mockito.when(mockResponse.getImdbRating()).thenReturn("N/A");

        Mockito.when(restClient.get()
                        .uri(Mockito.<Function<UriBuilder, URI>>any())
                        .retrieve()
                        .body(OmdbResponseDto.class))
                .thenReturn(mockResponse);

        omdbIntegrationService.fetchAndSaveRating(1L, "The Matrix");

        Mockito.verify(movieRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(movieRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void fetchAndSaveRating_Should_HandleException_And_NotCrash() {
        Mockito.when(restClient.get()).thenThrow(new RuntimeException("API is down"));

        Assertions.assertDoesNotThrow(() -> omdbIntegrationService.fetchAndSaveRating(1L, "The Matrix"));

        Mockito.verify(movieRepository, Mockito.never()).findById(Mockito.anyLong());
        Mockito.verify(movieRepository, Mockito.never()).save(Mockito.any());
    }
}