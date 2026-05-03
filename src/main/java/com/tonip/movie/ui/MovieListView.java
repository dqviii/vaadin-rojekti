package com.tonip.movie.ui;

import com.tonip.base.Broadcaster;
import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.GenreService;
import com.tonip.movie.MovieService;
import com.tonip.movie.domain.AgeRating;
import com.tonip.movie.domain.Genre;
import com.tonip.movie.domain.Movie;
import com.tonip.movie.history.MovieHistoryService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "movies", layout = MainLayout.class)
@PageTitle("Movies")
@Menu(order = 5, icon = "vaadin:film", title = "Movies")
@PermitAll
public class MovieListView extends VerticalLayout {

    private final MovieService movieService;
    private final GenreService genreService;
    private final Broadcaster broadcaster;
    private final MovieHistoryService historyService;
    private final Grid<Movie> grid = new Grid<>(Movie.class, false);
    private Registration broadcasterRegistration;

    public MovieListView(MovieService movieService, GenreService genreService,
                         Broadcaster broadcaster, MovieHistoryService historyService) {
        this.movieService = movieService;
        this.genreService = genreService;
        this.broadcaster = broadcaster;
        this.historyService = historyService;

        addClassName("movie-list-view");

        var newMovieBtn = new Button("New movie", new Icon(VaadinIcon.PLUS), e -> openEditor(new Movie()));
        newMovieBtn.addThemeVariants(ButtonVariant.PRIMARY);

        var viewTitle = new ViewTitle("Movies");
        var toolbar = new HorizontalLayout(viewTitle, newMovieBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(viewTitle);
        toolbar.getStyle()
                .set("border-bottom", "2px solid var(--lumo-primary-color)")
                .set("padding-bottom", "var(--lumo-space-s)");

        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        grid.addColumn(Movie::getTitle).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Movie::getDirectorName).setHeader("Director").setAutoWidth(true);
        grid.addColumn(m -> dateFormatter.format(m.getReleaseDate())).setHeader("Release date").setAutoWidth(true);
        grid.addColumn(m -> m.getAgeRating().getDisplayName()).setHeader("Rating").setAutoWidth(true);
        grid.addColumn(Movie::getOriginalLanguage).setHeader("Language").setAutoWidth(true);
        grid.addColumn(m -> m.getGenres().stream()
                .map(Genre::getGenreName)
                .collect(Collectors.joining(", ")))
                .setHeader("Genres").setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(this::createRowActions).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItems(query -> movieService.list(toSpringPageRequest(query)).stream());
        grid.setEmptyStateText("No movies yet. Click \"New movie\" to add one.");
        grid.setSizeFull();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(toolbar, grid);
    }

    private HorizontalLayout createRowActions(Movie movie) {
        var history = new Button(new Icon(VaadinIcon.CLOCK), e -> openHistory(movie));
        history.addThemeVariants(ButtonVariant.TERTIARY);
        history.getElement().setAttribute("aria-label", "History of " + movie.getTitle());
        history.getElement().setAttribute("title", "View revision history");

        var edit = new Button(new Icon(VaadinIcon.EDIT), e -> openEditor(movie));
        edit.addThemeVariants(ButtonVariant.TERTIARY);
        edit.getElement().setAttribute("aria-label", "Edit " + movie.getTitle());

        var delete = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(movie));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        delete.getElement().setAttribute("aria-label", "Delete " + movie.getTitle());

        var actions = new HorizontalLayout(history, edit, delete);
        actions.setSpacing(false);
        return actions;
    }

    private void openHistory(Movie movie) {
        new MovieHistoryDialog(movie, historyService, getLocale()).open();
    }

    private void openEditor(Movie movie) {
        boolean creating = movie.getId() == null;

        var dialog = new Dialog();
        dialog.setHeaderTitle(creating ? "New movie" : "Edit movie");
        dialog.setWidth("32rem");

        var title = new TextField("Title");
        title.setMaxLength(Movie.TITLE_MAX_LENGTH);
        title.setRequiredIndicatorVisible(true);

        var director = new TextField("Director");
        director.setMaxLength(Movie.DIRECTOR_MAX_LENGTH);
        director.setRequiredIndicatorVisible(true);

        var releaseDate = new DatePicker("Release date");
        releaseDate.setRequiredIndicatorVisible(true);

        var ageRating = new ComboBox<AgeRating>("Age rating");
        ageRating.setItems(AgeRating.values());
        ageRating.setItemLabelGenerator(AgeRating::getDisplayName);
        ageRating.setRequiredIndicatorVisible(true);

        var language = new TextField("Original language");
        language.setMaxLength(Movie.LANGUAGE_MAX_LENGTH);
        language.setRequiredIndicatorVisible(true);

        var genres = new MultiSelectComboBox<Genre>("Genres");
        genres.setItems(genreService.findAll());
        genres.setItemLabelGenerator(Genre::getGenreName);

        var form = new FormLayout(title, director, releaseDate, ageRating, language, genres);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("32em", 2));
        form.setColspan(genres, 2);

        var binder = new BeanValidationBinder<>(Movie.class);
        binder.forField(title).asRequired("Title is required").bind("title");
        binder.forField(director).asRequired("Director is required").bind("directorName");
        binder.forField(releaseDate).asRequired("Release date is required").bind("releaseDate");
        binder.forField(ageRating).asRequired("Age rating is required").bind("ageRating");
        binder.forField(language).asRequired("Language is required").bind("originalLanguage");
        binder.forField(genres)
                .bind(m -> new LinkedHashSet<>(m.getGenres()),
                        (m, v) -> m.setGenres(new LinkedHashSet<>(v)));
        binder.readBean(movie);

        var save = new Button("Save", e -> {
            try {
                binder.writeBean(movie);
                movieService.save(movie);
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show(creating ? "Movie added" : "Movie updated", 3000,
                        Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.SUCCESS);
            } catch (ValidationException ex) {
                Notification.show("Please fix the highlighted fields", 3000,
                        Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.PRIMARY);

        var cancel = new Button("Cancel", e -> dialog.close());

        dialog.add(form);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        var ui = attachEvent.getUI();
        broadcasterRegistration = broadcaster.register(topic -> {
            if (MovieService.TOPIC.equals(topic)) {
                ui.access(() -> grid.getDataProvider().refreshAll());
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }

    private void confirmDelete(Movie movie) {
        var confirm = new ConfirmDialog();
        confirm.setHeader("Delete movie");
        confirm.setText("Are you sure you want to delete \"" + movie.getTitle() + "\"? This cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            try {
                movieService.delete(movie.getId());
                grid.getDataProvider().refreshAll();
                Notification.show("Movie deleted", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.SUCCESS);
            } catch (DataIntegrityViolationException ex) {
                Notification.show(
                        "Cannot delete \"" + movie.getTitle()
                                + "\" — it still has stats or showtimes. Remove those first.",
                        6000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.ERROR);
            }
        });
        confirm.open();
    }
}
