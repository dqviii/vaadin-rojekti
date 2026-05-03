package com.tonip.movie;

import com.tonip.movie.domain.AgeRating;
import com.tonip.movie.domain.Genre;
import com.tonip.movie.domain.GenreRepository;
import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieRepository;
import com.tonip.movie.domain.MovieStats;
import com.tonip.movie.domain.MovieStatsRepository;
import com.tonip.movie.domain.ScreenType;
import com.tonip.movie.domain.Showtime;
import com.tonip.movie.domain.ShowtimeRepository;
import com.tonip.movie.domain.TargetAudience;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MovieCatalogSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final MovieStatsRepository movieStatsRepository;
    private final ShowtimeRepository showtimeRepository;

    public MovieCatalogSeeder(GenreRepository genreRepository,
                              MovieRepository movieRepository,
                              MovieStatsRepository movieStatsRepository,
                              ShowtimeRepository showtimeRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.movieStatsRepository = movieStatsRepository;
        this.showtimeRepository = showtimeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Genre> genres = seedGenres();
        seedMovies(genres);
        seedShowtimes();
    }

    private Map<String, Genre> seedGenres() {
        record GenreSpec(String name, String description, String iconCode,
                         boolean mainstream, TargetAudience audience) {
        }
        var specs = List.of(
                new GenreSpec("Drama", "Character-driven stories with emotional weight.",
                        "vaadin:masks", true, TargetAudience.ADULT),
                new GenreSpec("Action", "Fast-paced sequences, stunts, and conflict.",
                        "vaadin:bolt", true, TargetAudience.TEEN),
                new GenreSpec("Crime", "Heists, investigations, and the underworld.",
                        "vaadin:key", true, TargetAudience.ADULT),
                new GenreSpec("Sci-Fi", "Speculative futures and scientific premise.",
                        "vaadin:rocket", true, TargetAudience.GENERAL),
                new GenreSpec("Thriller", "Suspense, tension, and high stakes.",
                        "vaadin:fire", true, TargetAudience.ADULT),
                new GenreSpec("Romance", "Love stories and relationships at the core.",
                        "vaadin:heart", true, TargetAudience.GENERAL),
                new GenreSpec("Animation", "Fully animated feature productions.",
                        "vaadin:paintbrush", true, TargetAudience.FAMILY),
                new GenreSpec("Comedy", "Humor-driven plots and characters.",
                        "vaadin:smiley-o", true, TargetAudience.GENERAL),
                new GenreSpec("Family", "Suitable for all-ages family viewing.",
                        "vaadin:family", true, TargetAudience.FAMILY),
                new GenreSpec("Horror", "Fear, dread, and the supernatural.",
                        "vaadin:ghost", false, TargetAudience.ADULT));

        var byName = new LinkedHashMap<String, Genre>();
        for (var spec : specs) {
            if (genreRepository.existsByGenreNameIgnoreCase(spec.name())) {
                genreRepository.findAll().stream()
                        .filter(g -> g.getGenreName().equalsIgnoreCase(spec.name()))
                        .findFirst()
                        .ifPresent(g -> byName.put(spec.name(), g));
                continue;
            }
            var genre = new Genre(spec.name(), spec.description(), spec.iconCode(),
                    spec.mainstream(), spec.audience());
            byName.put(spec.name(), genreRepository.save(genre));
        }
        return byName;
    }

    private void seedMovies(Map<String, Genre> genres) {
        record MovieSpec(String title, String director, LocalDate released, AgeRating rating,
                         String language, List<String> genreNames,
                         BigDecimal budget, BigDecimal revenue,
                         int runtime, double imdb, int reviews) {
        }
        var specs = List.of(
                new MovieSpec("The Godfather", "Francis Ford Coppola", LocalDate.of(1972, 3, 24),
                        AgeRating.R, "English", List.of("Drama", "Crime"),
                        new BigDecimal("6000000"), new BigDecimal("250000000"),
                        175, 9.2, 1900000),
                new MovieSpec("The Dark Knight", "Christopher Nolan", LocalDate.of(2008, 7, 18),
                        AgeRating.PG_13, "English", List.of("Action", "Crime", "Drama"),
                        new BigDecimal("185000000"), new BigDecimal("1006000000"),
                        152, 9.0, 2800000),
                new MovieSpec("Inception", "Christopher Nolan", LocalDate.of(2010, 7, 16),
                        AgeRating.PG_13, "English", List.of("Sci-Fi", "Action", "Thriller"),
                        new BigDecimal("160000000"), new BigDecimal("836000000"),
                        148, 8.8, 2500000),
                new MovieSpec("Pulp Fiction", "Quentin Tarantino", LocalDate.of(1994, 10, 14),
                        AgeRating.R, "English", List.of("Crime", "Drama"),
                        new BigDecimal("8000000"), new BigDecimal("213000000"),
                        154, 8.9, 2200000),
                new MovieSpec("The Shawshank Redemption", "Frank Darabont", LocalDate.of(1994, 10, 14),
                        AgeRating.R, "English", List.of("Drama"),
                        new BigDecimal("25000000"), new BigDecimal("73000000"),
                        142, 9.3, 2900000),
                new MovieSpec("Forrest Gump", "Robert Zemeckis", LocalDate.of(1994, 7, 6),
                        AgeRating.PG_13, "English", List.of("Drama", "Romance"),
                        new BigDecimal("55000000"), new BigDecimal("678000000"),
                        142, 8.8, 2300000),
                new MovieSpec("The Matrix", "Lana Wachowski", LocalDate.of(1999, 3, 31),
                        AgeRating.R, "English", List.of("Sci-Fi", "Action"),
                        new BigDecimal("63000000"), new BigDecimal("467000000"),
                        136, 8.7, 2100000),
                new MovieSpec("Interstellar", "Christopher Nolan", LocalDate.of(2014, 11, 7),
                        AgeRating.PG_13, "English", List.of("Sci-Fi", "Drama"),
                        new BigDecimal("165000000"), new BigDecimal("773000000"),
                        169, 8.7, 2000000),
                new MovieSpec("Parasite", "Bong Joon-ho", LocalDate.of(2019, 5, 30),
                        AgeRating.R, "Korean", List.of("Thriller", "Drama"),
                        new BigDecimal("11400000"), new BigDecimal("258000000"),
                        132, 8.5, 900000),
                new MovieSpec("Spirited Away", "Hayao Miyazaki", LocalDate.of(2001, 7, 20),
                        AgeRating.PG, "Japanese", List.of("Animation", "Family"),
                        new BigDecimal("19000000"), new BigDecimal("395000000"),
                        125, 8.6, 800000));

        for (var spec : specs) {
            if (movieRepository.existsByTitleIgnoreCase(spec.title())) {
                continue;
            }
            var movie = new Movie(spec.title(), spec.director(), spec.released(),
                    spec.rating(), spec.language());
            Set<Genre> linked = new LinkedHashSet<>();
            for (String name : spec.genreNames()) {
                Genre g = genres.get(name);
                if (g != null) {
                    linked.add(g);
                }
            }
            movie.setGenres(linked);
            Movie saved = movieRepository.save(movie);

            if (!movieStatsRepository.existsByMovieId(saved.getId())) {
                var stats = new MovieStats(saved, spec.budget(), spec.revenue(),
                        spec.runtime(), spec.imdb(), spec.reviews());
                movieStatsRepository.save(stats);
            }
        }
    }

    private void seedShowtimes() {
        record ShowtimeSpec(String movieTitle, LocalDateTime start, String hall,
                            ScreenType screen, BigDecimal price, int seats) {
        }
        var specs = List.of(
                new ShowtimeSpec("Pulp Fiction", LocalDateTime.of(2026, 5, 8, 19, 30),
                        "Hall A", ScreenType.IMAX, new BigDecimal("14.50"), 120),
                new ShowtimeSpec("Spirited Away", LocalDateTime.of(2026, 5, 22, 17, 0),
                        "Hall B", ScreenType.STANDARD_2D, new BigDecimal("12.00"), 200),
                new ShowtimeSpec("The Matrix", LocalDateTime.of(2026, 5, 15, 21, 0),
                        "Hall C", ScreenType.DOLBY_ATMOS, new BigDecimal("16.50"), 150));

        for (var spec : specs) {
            Movie movie = movieRepository.findAll().stream()
                    .filter(m -> m.getTitle().equalsIgnoreCase(spec.movieTitle()))
                    .findFirst()
                    .orElse(null);
            if (movie == null || showtimeRepository.existsByMovieId(movie.getId())) {
                continue;
            }
            showtimeRepository.save(new Showtime(movie, spec.start(), spec.hall(),
                    spec.screen(), spec.price(), spec.seats()));
        }
    }
}
