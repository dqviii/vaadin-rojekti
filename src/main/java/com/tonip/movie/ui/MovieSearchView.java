package com.tonip.movie.ui;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.MovieSearchCriteria;
import com.tonip.movie.MovieSearchService;
import com.tonip.movie.domain.AgeRating;
import com.tonip.movie.domain.Movie;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route(value = "movies/search", layout = MainLayout.class)
@PageTitle("Search movies")
@Menu(order = 6, icon = "vaadin:search", title = "Search movies")
@PermitAll
public class MovieSearchView extends VerticalLayout {

    private final MovieSearchService searchService;

    private final TextField titleField = new TextField("Title contains");
    private final TextField directorField = new TextField("Director contains");
    private final ComboBox<AgeRating> ratingField = new ComboBox<>("Age rating");
    private final Grid<Movie> resultGrid = new Grid<>(Movie.class, false);

    private MovieSearchCriteria criteria = MovieSearchCriteria.empty();

    public MovieSearchView(MovieSearchService searchService) {
        this.searchService = searchService;

        addClassName("movie-search-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        ratingField.setItems(AgeRating.values());
        ratingField.setItemLabelGenerator(AgeRating::getDisplayName);
        ratingField.setClearButtonVisible(true);
        titleField.setClearButtonVisible(true);
        directorField.setClearButtonVisible(true);
        titleField.setWidthFull();
        directorField.setWidthFull();
        ratingField.setWidthFull();

        var searchBtn = new Button("Search", new Icon(VaadinIcon.SEARCH), e -> applyFilters());
        searchBtn.addThemeVariants(ButtonVariant.PRIMARY);
        searchBtn.setWidthFull();

        var resetBtn = new Button("Reset", e -> resetFilters());
        resetBtn.setWidthFull();

        var filtersPanel = new VerticalLayout(
                new H3("Filters"),
                titleField, directorField, ratingField,
                searchBtn, resetBtn);
        filtersPanel.addClassName("movie-search-filters");
        filtersPanel.setPadding(false);
        filtersPanel.setSpacing(true);

        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
        resultGrid.addColumn(Movie::getTitle).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        resultGrid.addColumn(Movie::getDirectorName).setHeader("Director").setAutoWidth(true);
        resultGrid.addColumn(m -> dateFormatter.format(m.getReleaseDate())).setHeader("Release date").setAutoWidth(true);
        resultGrid.addColumn(m -> m.getAgeRating().getDisplayName()).setHeader("Rating").setAutoWidth(true);
        resultGrid.addColumn(Movie::getOriginalLanguage).setHeader("Language").setAutoWidth(true);
        resultGrid.setItems(query -> searchService.search(criteria, query.getOffset(), query.getLimit()).stream());
        resultGrid.setEmptyStateText("No movies match the current filters.");
        resultGrid.setSizeFull();

        var resultsPanel = new VerticalLayout(new ViewTitle("Search results"), resultGrid);
        resultsPanel.addClassName("movie-search-results");
        resultsPanel.setPadding(false);
        resultsPanel.setSpacing(true);
        resultsPanel.setSizeFull();
        resultsPanel.expand(resultGrid);

        var split = new SplitLayout(filtersPanel, resultsPanel);
        split.addClassName("movie-search-split");
        split.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        split.setSplitterPosition(28);
        split.setSizeFull();

        var header = new HorizontalLayout(new ViewTitle("Search movies"));
        header.setPadding(true);

        add(header, split);
        expand(split);
    }

    private void applyFilters() {
        criteria = new MovieSearchCriteria(
                titleField.getValue(),
                directorField.getValue(),
                ratingField.getValue());
        resultGrid.getDataProvider().refreshAll();
    }

    private void resetFilters() {
        titleField.clear();
        directorField.clear();
        ratingField.clear();
        criteria = MovieSearchCriteria.empty();
        resultGrid.getDataProvider().refreshAll();
    }
}
