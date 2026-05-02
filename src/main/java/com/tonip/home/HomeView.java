package com.tonip.home;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.GenreService;
import com.tonip.movie.MovieService;
import com.tonip.movie.MovieStatsService;
import com.tonip.movie.ShowtimeService;
import com.tonip.movie.domain.Showtime;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@Menu(order = 0, icon = "vaadin:home", title = "Home")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView(MovieService movieService,
                    ShowtimeService showtimeService,
                    GenreService genreService,
                    MovieStatsService movieStatsService) {
        addClassName("home-view");
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);

        var hero = new Div();
        hero.addClassName("home-hero");
        hero.add(new ViewTitle(getTranslation("home.title")));
        hero.add(new Paragraph(getTranslation("home.intro")));

        var statsRow = new Div();
        statsRow.addClassName("home-stats");
        statsRow.add(
                statCard(VaadinIcon.FILM, movieService.count(), getTranslation("home.stat.movies")),
                statCard(VaadinIcon.TICKET, showtimeService.count(), getTranslation("home.stat.showtimes")),
                statCard(VaadinIcon.TAGS, genreService.count(), getTranslation("home.stat.genres")),
                statCard(VaadinIcon.CHART, movieStatsService.count(), getTranslation("home.stat.stats")));

        var recentSection = new Div();
        recentSection.addClassName("home-recent");
        recentSection.add(new H2(getTranslation("home.recent.title")));

        List<Showtime> recent = showtimeService.findRecent(5);
        if (recent.isEmpty()) {
            recentSection.add(new Paragraph(getTranslation("home.recent.empty")));
        } else {
            var formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    .withLocale(getLocale());
            var list = new Div();
            list.addClassName("home-recent-list");
            for (Showtime s : recent) {
                var row = new Div();
                row.addClassName("home-recent-row");
                var when = new Span(formatter.format(s.getStartTime()));
                when.addClassName("home-recent-when");
                var title = new Span(s.getMovie().getTitle());
                title.addClassName("home-recent-title");
                var hall = new Span(s.getTheaterHall() + " · " + s.getScreenType().getDisplayName());
                hall.addClassName("home-recent-hall");
                row.add(when, title, hall);
                list.add(row);
            }
            recentSection.add(list);
        }

        add(hero, statsRow, recentSection);
    }

    private Div statCard(VaadinIcon iconType, long value, String label) {
        var card = new Div();
        card.addClassName("home-stat-card");

        var icon = new Icon(iconType);
        icon.addClassName("home-stat-icon");

        var number = new Span(Long.toString(value));
        number.addClassName("home-stat-number");

        var caption = new Span(label);
        caption.addClassName("home-stat-label");

        card.add(icon, number, caption);
        return card;
    }
}
