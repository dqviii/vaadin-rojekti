package com.tonip.movie;

import com.tonip.movie.domain.Genre;
import com.tonip.movie.domain.GenreRepository;
import com.tonip.movie.domain.MovieRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    GenreService(GenreRepository genreRepository, MovieRepository movieRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional
    public Genre save(Genre genre) {
        return genreRepository.saveAndFlush(genre);
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.deleteGenreLinks(id);
        genreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Genre> findById(Long id) {
        return genreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Genre> list(Pageable pageable) {
        Pageable effective = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.ASC, "genreName"))
                : pageable;
        return genreRepository.findAllBy(effective).toList();
    }

    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreRepository.findAll(Sort.by(Sort.Direction.ASC, "genreName"));
    }

    @Transactional(readOnly = true)
    public long count() {
        return genreRepository.count();
    }
}
