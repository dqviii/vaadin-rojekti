package com.tonip.movie.domain;

import com.tonip.base.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.envers.Audited;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Entity
@Audited
@Table(name = "movie_stats")
public class MovieStats extends AuditableEntity {

    public static final int RUNTIME_MAX_MINUTES = 600;
    public static final String IMDB_MIN = "0.0";
    public static final String IMDB_MAX = "10.0";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_stats_id")
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "movie_id", nullable = false, unique = true)
    private Movie movie;

    @NotNull
    @PositiveOrZero
    @Column(name = "budget", nullable = false, precision = 14, scale = 2)
    private BigDecimal budget;

    @NotNull
    @PositiveOrZero
    @Column(name = "box_office_revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal boxOfficeRevenue;

    @NotNull
    @Positive
    @Max(RUNTIME_MAX_MINUTES)
    @Column(name = "runtime_minutes", nullable = false)
    private Integer runtimeMinutes;

    @NotNull
    @DecimalMin(IMDB_MIN)
    @DecimalMax(IMDB_MAX)
    @Column(name = "imdb_rating", nullable = false)
    private Double imdbRating;

    @NotNull
    @PositiveOrZero
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    public MovieStats() {
    }

    public MovieStats(Movie movie, BigDecimal budget, BigDecimal boxOfficeRevenue,
                      Integer runtimeMinutes, Double imdbRating, Integer reviewCount) {
        this.movie = movie;
        this.budget = budget;
        this.boxOfficeRevenue = boxOfficeRevenue;
        this.runtimeMinutes = runtimeMinutes;
        this.imdbRating = imdbRating;
        this.reviewCount = reviewCount;
    }

    public Long getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getBoxOfficeRevenue() {
        return boxOfficeRevenue;
    }

    public void setBoxOfficeRevenue(BigDecimal boxOfficeRevenue) {
        this.boxOfficeRevenue = boxOfficeRevenue;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public void setRuntimeMinutes(Integer runtimeMinutes) {
        this.runtimeMinutes = runtimeMinutes;
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(Double imdbRating) {
        this.imdbRating = imdbRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MovieStats other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
