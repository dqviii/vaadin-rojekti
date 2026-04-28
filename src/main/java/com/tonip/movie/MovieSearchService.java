package com.tonip.movie;

import com.tonip.movie.domain.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
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
        var movie = cq.from(Movie.class);

        var predicates = new ArrayList<Predicate>();

        if (criteria.title() != null && !criteria.title().isBlank()) {
            predicates.add(cb.like(cb.lower(movie.get("title")),
                    "%" + criteria.title().toLowerCase() + "%"));
        }
        if (criteria.directorName() != null && !criteria.directorName().isBlank()) {
            predicates.add(cb.like(cb.lower(movie.get("directorName")),
                    "%" + criteria.directorName().toLowerCase() + "%"));
        }
        if (criteria.ageRating() != null) {
            predicates.add(cb.equal(movie.get("ageRating"), criteria.ageRating()));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(Predicate[]::new)));
        }
        cq.orderBy(cb.asc(movie.get("title")));

        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
