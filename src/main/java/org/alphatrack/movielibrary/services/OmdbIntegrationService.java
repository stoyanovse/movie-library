package org.alphatrack.movielibrary.services;

import lombok.extern.slf4j.Slf4j;
import org.alphatrack.movielibrary.dtos.OmdbResponseDto;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OmdbIntegrationService {

    public static final String RATING_FETCH_FOR_MOVIE = "Starting async rating fetch for movie: {}";
    public static final String ADDED_RATING_TO_MOVIE = "Successfully added rating {} to movie {}";
    public static final String NOT_FOUND_ON_OMDB_OR_NO_RATING_AVAILABLE_FOR = "Movie not found on OMDb or no rating available for: {}";
    public static final String FAILED_TO_FETCH_RATING_FOR_MOVIE_REASON = "Failed to fetch rating for movie: {}. Reason: {}";
    private final RestClient restClient;
    private final MovieRepository movieRepository;
    private final String apiKey;

    @Autowired
    public OmdbIntegrationService(
            RestClient.Builder restClientBuilder,
            MovieRepository movieRepository,
            @Value("${omdb.api.key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("http://www.omdbapi.com/").build();
        this.movieRepository = movieRepository;
        this.apiKey = apiKey;
    }
    //magics strings constants
    @Async
    public void fetchAndSaveRating(Long movieId, String title) {
        log.info(RATING_FETCH_FOR_MOVIE, title);
        try {
            OmdbResponseDto responseDto = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("t", title)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .body(OmdbResponseDto.class);
            if (responseDto != null && responseDto.getResponse().equalsIgnoreCase("True")
            && responseDto.getImdbRating() != null && !responseDto.getImdbRating().equalsIgnoreCase("N/A")) {
                Double parsedRating = Double.parseDouble(responseDto.getImdbRating());

                movieRepository.findById(movieId).ifPresent(movie -> {
                    movie.setRating(parsedRating);
                    movieRepository.save(movie);
                    log.info(ADDED_RATING_TO_MOVIE, parsedRating, title);
                });
            } else {
                log.warn(NOT_FOUND_ON_OMDB_OR_NO_RATING_AVAILABLE_FOR, title);
            }
        } catch (Exception e) {
            log.error(FAILED_TO_FETCH_RATING_FOR_MOVIE_REASON, title, e.getMessage());
        }
    }
}
