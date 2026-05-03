package com.tonip.movie.history;

import com.tonip.movie.domain.Movie;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieHistoryService {

    private final EntityManager entityManager;

    MovieHistoryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<MovieRevision> findHistory(Long movieId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(Movie.class, false, true)
                .add(AuditEntity.id().eq(movieId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return rows.stream()
                .map(row -> MovieRevision.of(
                        (Movie) row[0],
                        (DefaultRevisionEntity) row[1],
                        (RevisionType) row[2]))
                .toList();
    }
}
