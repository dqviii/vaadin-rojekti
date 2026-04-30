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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtime")
public class Showtime {

    public static final int THEATER_HALL_MAX_LENGTH = 50;
    public static final int AVAILABLE_SEATS_MAX = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showtime_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotBlank
    @Size(max = THEATER_HALL_MAX_LENGTH)
    @Column(name = "theater_hall", nullable = false, length = THEATER_HALL_MAX_LENGTH)
    private String theaterHall = "";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false, length = 20)
    private ScreenType screenType;

    @NotNull
    @PositiveOrZero
    @Column(name = "ticket_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal ticketPrice;

    @NotNull
    @PositiveOrZero
    @Max(AVAILABLE_SEATS_MAX)
    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    public Showtime() {
    }

    public Showtime(Movie movie, LocalDateTime startTime, String theaterHall,
                    ScreenType screenType, BigDecimal ticketPrice, Integer availableSeats) {
        this.movie = movie;
        this.startTime = startTime;
        this.theaterHall = theaterHall;
        this.screenType = screenType;
        this.ticketPrice = ticketPrice;
        this.availableSeats = availableSeats;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getTheaterHall() {
        return theaterHall;
    }

    public void setTheaterHall(String theaterHall) {
        this.theaterHall = theaterHall;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public void setScreenType(ScreenType screenType) {
        this.screenType = screenType;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Showtime other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
