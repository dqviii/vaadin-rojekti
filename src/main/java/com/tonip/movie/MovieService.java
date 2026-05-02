package com.tonip.movie;

import com.tonip.base.Broadcaster;
import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    public static final String TOPIC = "movies";

    private final MovieRepository movieRepository;
    private final Broadcaster broadcaster;

    MovieService(MovieRepository movieRepository, Broadcaster broadcaster) {
        this.movieRepository = movieRepository;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public Movie save(Movie movie) {
        Movie saved = movieRepository.saveAndFlush(movie);
        broadcaster.broadcast(TOPIC);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.deleteById(id);
        broadcaster.broadcast(TOPIC);
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Movie> list(Pageable pageable) {
        return movieRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public long count() {
        return movieRepository.count();
    }
}
