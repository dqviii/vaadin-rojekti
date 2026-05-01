package com.tonip.movie;

import com.tonip.movie.domain.AgeRating;
import com.tonip.movie.domain.Genre;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public record MovieSearchCriteria(
        @Nullable String searchText,
        @Nullable AgeRating ageRating,
        @Nullable LocalDate releaseDateFrom,
        @Nullable LocalDate releaseDateTo,
        @Nullable Genre genre,
        @Nullable String genreNameContains) {

    public static MovieSearchCriteria empty() {
        return new MovieSearchCriteria(null, null, null, null, null, null);
    }
}
