package com.tonip.movie;

import com.tonip.movie.domain.AgeRating;
import org.jspecify.annotations.Nullable;

public record MovieSearchCriteria(
        @Nullable String title,
        @Nullable String directorName,
        @Nullable AgeRating ageRating) {

    public static MovieSearchCriteria empty() {
        return new MovieSearchCriteria(null, null, null);
    }
}
