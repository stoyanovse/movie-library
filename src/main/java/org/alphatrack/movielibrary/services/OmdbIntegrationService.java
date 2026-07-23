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

    @Async
    public void fetchAndSaveRating(Long movieId, String title) {
        log.info("Starting async rating fetch for movie: {}", title);
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
                    log.info("Successfully added rating {} to movie {}", parsedRating, title);
                });
            } else {
                log.warn("Movie not found on OMDb or no rating available for: {}", title);
            }
        } catch (Exception e) {
            log.error("Failed to fetch rating for movie: {}. Reason: {}" , title, e.getMessage());
        }
    }
}
