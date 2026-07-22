package org.alphatrack.movielibrary.dtos.filters;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
public class MovieFilterOptions {

    private Optional<String> title;
    private Optional<String> director;
    private Optional<Integer> releaseYear;
    private Optional<Double> rating;
    private Optional<String> sortBy;
    private Optional<String> sortOrder;

    @Builder
    public MovieFilterOptions(String title,
                              String director,
                              Integer releaseYear,
                              Double rating,
                              String sortBy,
                              String sortOrder) {
        this.title = Optional.ofNullable(title);
        this.director = Optional.ofNullable(director);
        this.releaseYear = Optional.ofNullable(releaseYear);
        this.rating = Optional.ofNullable(rating);
        this.sortBy = Optional.ofNullable(sortBy);
        this.sortOrder = Optional.ofNullable(sortOrder);
    }
}
