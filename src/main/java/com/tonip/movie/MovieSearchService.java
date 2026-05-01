package com.tonip.movie;

import com.tonip.movie.domain.Genre;
import com.tonip.movie.domain.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieSearchService {

    private final EntityManager entityManager;

    MovieSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Movie> search(MovieSearchCriteria criteria, int offset, int limit) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Movie.class);
        Root<Movie> movie = cq.from(Movie.class);

        var predicates = new ArrayList<Predicate>();
        boolean needsDistinct = false;

        // B5: (title LIKE :q OR directorName LIKE :q) AND ...
        if (criteria.searchText() != null && !criteria.searchText().isBlank()) {
            var pattern = "%" + criteria.searchText().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(movie.get("title")), pattern),
                    cb.like(cb.lower(movie.get("directorName")), pattern)));
        }

        if (criteria.ageRating() != null) {
            predicates.add(cb.equal(movie.get("ageRating"), criteria.ageRating()));
        }

        // B2: date range filter
        var from = criteria.releaseDateFrom();
        var to = criteria.releaseDateTo();
        if (from != null && to != null) {
            predicates.add(cb.between(movie.get("releaseDate"), from, to));
        } else if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(movie.get("releaseDate"), from));
        } else if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(movie.get("releaseDate"), to));
        }

        // B3 / B4: filter via joined genres relation
        boolean filterByGenre = criteria.genre() != null;
        boolean filterByGenreName = criteria.genreNameContains() != null
                && !criteria.genreNameContains().isBlank();
        if (filterByGenre || filterByGenreName) {
            Join<Movie, Genre> genreJoin = movie.join("genres");
            if (filterByGenre) {
                predicates.add(cb.equal(genreJoin.get("id"), criteria.genre().getId()));
            }
            if (filterByGenreName) {
                predicates.add(cb.like(cb.lower(genreJoin.get("genreName")),
                        "%" + criteria.genreNameContains().toLowerCase() + "%"));
            }
            needsDistinct = true;
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(Predicate[]::new)));
        }
        cq.orderBy(cb.asc(movie.get("title")));
        if (needsDistinct) {
            cq.distinct(true);
        }

        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
