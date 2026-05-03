package com.tonip.movie.history;

import com.tonip.movie.domain.Movie;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.DefaultRevisionEntity;

import java.time.Instant;

public record MovieRevision(
        int revisionNumber,
        Instant revisionDate,
        RevisionType revisionType,
        String title,
        String directorName,
        String updatedBy) {

    public static MovieRevision of(Movie snapshot, DefaultRevisionEntity rev, RevisionType type) {
        String auditor = null;
        if (snapshot != null) {
            auditor = (type == RevisionType.ADD)
                    ? snapshot.getCreatedBy()
                    : (snapshot.getUpdatedBy() != null
                            ? snapshot.getUpdatedBy()
                            : snapshot.getCreatedBy());
        }
        return new MovieRevision(
                rev.getId(),
                rev.getRevisionDate().toInstant(),
                type,
                snapshot != null ? snapshot.getTitle() : null,
                snapshot != null ? snapshot.getDirectorName() : null,
                auditor);
    }

    public String typeLabel() {
        return switch (revisionType) {
            case ADD -> "Created";
            case MOD -> "Updated";
            case DEL -> "Deleted";
        };
    }
}
