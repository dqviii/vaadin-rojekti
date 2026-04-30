package com.tonip.movie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movie")
public class Movie {

    public static final int TITLE_MAX_LENGTH = 200;
    public static final int DIRECTOR_MAX_LENGTH = 120;
    public static final int LANGUAGE_MAX_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @NotBlank
    @Size(max = TITLE_MAX_LENGTH)
    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title = "";

    @NotBlank
    @Size(max = DIRECTOR_MAX_LENGTH)
    @Column(name = "director_name", nullable = false, length = DIRECTOR_MAX_LENGTH)
    private String directorName = "";

    @NotNull
    @PastOrPresent
    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false, length = 10)
    private AgeRating ageRating;

    @NotBlank
    @Size(max = LANGUAGE_MAX_LENGTH)
    @Column(name = "original_language", nullable = false, length = LANGUAGE_MAX_LENGTH)
    private String originalLanguage = "";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new LinkedHashSet<>();

    public Movie() {
    }

    public Movie(String title, String directorName, LocalDate releaseDate,
                 AgeRating ageRating, String originalLanguage) {
        this.title = title;
        this.directorName = directorName;
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.originalLanguage = originalLanguage;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public AgeRating getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(AgeRating ageRating) {
        this.ageRating = ageRating;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Movie other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
