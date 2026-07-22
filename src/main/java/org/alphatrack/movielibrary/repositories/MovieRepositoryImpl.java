package org.alphatrack.movielibrary.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.alphatrack.movielibrary.dtos.filters.MovieFilterOptions;
import org.alphatrack.movielibrary.models.Movie;
import org.alphatrack.movielibrary.repositories.contracts.MovieRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MovieRepositoryImpl implements MovieRepositoryCustom {
    private final EntityManager entityManager;

    @Autowired
    public MovieRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Movie> findAll(MovieFilterOptions movieFilterOptions) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
            Root<Movie> userRoot = cq.from(Movie.class);

            List<Predicate> predicates = new ArrayList<>();

            movieFilterOptions.getTitle().ifPresent(title ->
                    predicates.add(cb.like(userRoot.get("title"),"%" + title + "%")));

            movieFilterOptions.getDirector().ifPresent(director ->
                    predicates.add(cb.like(userRoot.get("director"), "%" + director + "%")));

            movieFilterOptions.getReleaseYear().ifPresent(releaseYear ->
                    predicates.add(cb.equal(userRoot.get("releaseYear"), releaseYear)));

            movieFilterOptions.getRating().ifPresent(rating ->
                    predicates.add(cb.greaterThanOrEqualTo(userRoot.get("rating"), rating)));

            cq.where(cb.and(predicates.toArray(new Predicate[0])));

            movieFilterOptions.getSortBy().ifPresent(sortBy -> {
                String sortOrder = movieFilterOptions.getSortOrder().orElse("asc");
                if (sortOrder.equalsIgnoreCase("desc")) {
                    cq.orderBy(cb.desc(userRoot.get((sortBy))));
                } else {
                    cq.orderBy(cb.asc(userRoot.get(sortBy)));
                }
            });

            return entityManager.createQuery(cq).getResultList();

    }
}
