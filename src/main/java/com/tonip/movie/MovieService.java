package com.tonip.movie;

import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Movie save(Movie movie) {
        return movieRepository.saveAndFlush(movie);
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Movie> list(Pageable pageable) {
        return movieRepository.findAllBy(pageable).toList();
    }
}
