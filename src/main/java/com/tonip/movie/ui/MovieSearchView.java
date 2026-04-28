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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        ratingField.setItems(AgeRating.values());
        ratingField.setItemLabelGenerator(AgeRating::getDisplayName);
        ratingField.setClearButtonVisible(true);
        titleField.setClearButtonVisible(true);
        directorField.setClearButtonVisible(true);

        var searchBtn = new Button("Search", new Icon(VaadinIcon.SEARCH), e -> applyFilters());
        searchBtn.addThemeVariants(ButtonVariant.PRIMARY);

        var resetBtn = new Button("Reset", e -> resetFilters());

        var filters = new HorizontalLayout(titleField, directorField, ratingField, searchBtn, resetBtn);
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setWrap(true);
        filters.setWidthFull();

        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
        resultGrid.addColumn(Movie::getTitle).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        resultGrid.addColumn(Movie::getDirectorName).setHeader("Director").setAutoWidth(true);
        resultGrid.addColumn(m -> dateFormatter.format(m.getReleaseDate())).setHeader("Release date").setAutoWidth(true);
        resultGrid.addColumn(m -> m.getAgeRating().getDisplayName()).setHeader("Rating").setAutoWidth(true);
        resultGrid.addColumn(Movie::getOriginalLanguage).setHeader("Language").setAutoWidth(true);
        resultGrid.setItems(query -> searchService.search(criteria, query.getOffset(), query.getLimit()).stream());
        resultGrid.setEmptyStateText("No movies match the current filters.");
        resultGrid.setSizeFull();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(new ViewTitle("Search movies"), filters, resultGrid);
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
