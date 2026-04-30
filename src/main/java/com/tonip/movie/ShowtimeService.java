package com.tonip.movie;

import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieRepository;
import com.tonip.movie.domain.Showtime;
import com.tonip.movie.domain.ShowtimeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Showtime save(Showtime showtime) {
        return showtimeRepository.saveAndFlush(showtime);
    }

    @Transactional
    public void delete(Long id) {
        showtimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Showtime> findById(Long id) {
        return showtimeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Showtime> list(Pageable pageable) {
        Pageable effective = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "startTime"))
                : pageable;
        return showtimeRepository.findAllBy(effective).toList();
    }

    @Transactional(readOnly = true)
    public List<Movie> allMovies() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long count() {
        return showtimeRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Showtime> findRecent(int limit) {
        return showtimeRepository.findAllBy(PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "startTime"))).toList();
    }
}
