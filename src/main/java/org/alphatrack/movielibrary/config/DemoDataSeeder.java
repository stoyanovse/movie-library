package org.alphatrack.movielibrary.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepository;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.alphatrack.movielibrary.services.OmdbIntegrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;
    private final OmdbIntegrationService omdbIntegrationService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);

        userRepository.save(User.builder()
                .username("admin")
                .firstName("Alex")
                .lastName("Admin")
                .email("admin@movielibrary.io")
                .password(encodedPassword)
                .role(Role.ADMIN)
                .isBlocked(false)
                .isEnabled(true)
                .build());

        userRepository.save(User.builder()
                .username("demo")
                .firstName("Demo")
                .lastName("User")
                .email("demo@movielibrary.io")
                .password(encodedPassword)
                .role(Role.USER)
                .isBlocked(false)
                .isEnabled(true)
                .build());

        List<Movie> seededMovies = movieRepository.saveAll(List.of(
                Movie.builder().title("Inception").director("Christopher Nolan").releaseYear(2010).build(),
                Movie.builder().title("The Godfather").director("Francis Ford Coppola").releaseYear(1972).build(),
                Movie.builder().title("Interstellar").director("Christopher Nolan").releaseYear(2014).build(),
                Movie.builder().title("Parasite").director("Bong Joon-ho").releaseYear(2019).build()
        ));


        seededMovies.forEach(movie ->
                omdbIntegrationService.fetchAndSaveRating(movie.getId(), movie.getTitle()));

        log.info("Demo data seeded: {} users, {} movies (ratings fetching asynchronously)",
                2, seededMovies.size());
    }
}
