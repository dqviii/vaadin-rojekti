package com.tonip.movie;

import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieRepository;
import com.tonip.movie.domain.MovieStats;
import com.tonip.movie.domain.MovieStatsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieStatsService {

    private final MovieStatsRepository movieStatsRepository;
    private final MovieRepository movieRepository;

    MovieStatsService(MovieStatsRepository movieStatsRepository, MovieRepository movieRepository) {
        this.movieStatsRepository = movieStatsRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public MovieStats save(MovieStats stats) {
        return movieStatsRepository.saveAndFlush(stats);
    }

    @Transactional
    public void delete(Long id) {
        movieStatsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<MovieStats> findById(Long id) {
        return movieStatsRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MovieStats> list(Pageable pageable) {
        return movieStatsRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public List<Movie> moviesWithoutStats() {
        return movieRepository.findMoviesWithoutStats();
    }

    @Transactional(readOnly = true)
    public long count() {
        return movieStatsRepository.count();
    }
}
