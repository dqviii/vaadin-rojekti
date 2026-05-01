package com.tonip.movie.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    Slice<Movie> findAllBy(Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    @Query("select m from Movie m where m.id not in (select s.movie.id from MovieStats s)")
    List<Movie> findMoviesWithoutStats();

    @Query("select m from Movie m join m.genres g where g.id = :genreId")
    List<Movie> findAllByGenreId(Long genreId);

    @Modifying
    @Query(value = "delete from movie_genre where genre_id = :genreId", nativeQuery = true)
    void deleteGenreLinks(Long genreId);
}
