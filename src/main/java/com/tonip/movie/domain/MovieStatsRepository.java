package com.tonip.movie.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieStatsRepository extends JpaRepository<MovieStats, Long> {

    Slice<MovieStats> findAllBy(Pageable pageable);

    boolean existsByMovieId(Long movieId);
}
