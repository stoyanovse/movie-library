package org.alphatrack.movielibrary.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.alphatrack.movielibrary.dtos.MovieRequestDto;
import org.alphatrack.movielibrary.dtos.MovieResponseDto;
import org.alphatrack.movielibrary.dtos.MovieUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.security.CustomUserDetails;
import org.alphatrack.movielibrary.services.contracts.MovieService;
import org.alphatrack.movielibrary.utils.mappers.MovieMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieRestController {
    private final MovieService movieService;
    private final MovieMapper movieMapper;

    public MovieRestController(MovieService movieService, MovieMapper movieMapper) {
        this.movieService = movieService;
        this.movieMapper = movieMapper;
    }

    @Operation(summary = "search all movies based on criteria")
    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getAll(@ModelAttribute MovieFilterOptions movieFilterOptions) {
        List<MovieResponseDto> list = movieMapper.movieListToResponseDtoList(movieService.getAll(movieFilterOptions));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = "search specific movie based on ID")
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponseDto> getById(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails currentUser) {
        MovieResponseDto movieResponseDto = movieMapper.movieToDto(movieService.getById(id, currentUser.getUser()));
        return new ResponseEntity<>(movieResponseDto,HttpStatus.OK);
    }

    @Operation(summary = "add new movie to the library.ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MovieResponseDto> create(@Valid @RequestBody MovieRequestDto movieRequestDto,
                                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        MovieResponseDto movieResponseDto = movieMapper.movieToDto(movieService.create(movieRequestDto, currentUser.getUser()));
        return new ResponseEntity<>(movieResponseDto,HttpStatus.CREATED);
    }

    @Operation(summary = "update a movie.ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponseDto> update(@Valid @RequestBody MovieUpdateDto movieUpdateDto,
                                                   @PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails currentUser) {
        MovieResponseDto movieResponseDto = movieMapper.movieToDto(movieService.update(id, movieUpdateDto, currentUser.getUser()));
        return new ResponseEntity<>(movieResponseDto,HttpStatus.OK);
    }

    @Operation(summary = "delete a movie.ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails currentUser) {
        movieService.delete(id, currentUser.getUser());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
