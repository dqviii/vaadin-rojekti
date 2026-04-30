package com.tonip.movie.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Slice<Genre> findAllBy(Pageable pageable);

    boolean existsByGenreNameIgnoreCase(String genreName);
}
