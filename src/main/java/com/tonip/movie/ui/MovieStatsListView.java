package com.tonip.movie.ui;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.MovieStatsService;
import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.MovieStats;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.util.Collections;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "movie-stats", layout = MainLayout.class)
@PageTitle("Movie stats")
@Menu(order = 10, icon = "vaadin:chart", title = "Movie stats")
@PermitAll
public class MovieStatsListView extends VerticalLayout {

    private final MovieStatsService movieStatsService;
    private final Grid<MovieStats> grid = new Grid<>(MovieStats.class, false);

    public MovieStatsListView(MovieStatsService movieStatsService) {
        this.movieStatsService = movieStatsService;

        var newBtn = new Button("New stats", new Icon(VaadinIcon.PLUS), e -> openEditor(new MovieStats()));
        newBtn.addThemeVariants(ButtonVariant.PRIMARY);

        var viewTitle = new ViewTitle("Movie stats");
        var toolbar = new HorizontalLayout(viewTitle, newBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(viewTitle);

        var currencyFormat = NumberFormat.getCurrencyInstance(getLocale());

        grid.addColumn(s -> s.getMovie().getTitle()).setHeader("Movie").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(s -> s.getRuntimeMinutes() + " min").setHeader("Runtime").setAutoWidth(true);
        grid.addColumn(s -> String.format("%.1f / 10", s.getImdbRating())).setHeader("IMDB").setAutoWidth(true);
        grid.addColumn(s -> currencyFormat.format(s.getBudget())).setHeader("Budget").setAutoWidth(true);
        grid.addColumn(s -> currencyFormat.format(s.getBoxOfficeRevenue())).setHeader("Box office").setAutoWidth(true);
        grid.addColumn(MovieStats::getReviewCount).setHeader("Reviews").setAutoWidth(true);
        grid.addComponentColumn(this::createRowActions).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItems(query -> movieStatsService.list(toSpringPageRequest(query)).stream());
        grid.setEmptyStateText("No stats yet. Click \"New stats\" to add one.");
        grid.setSizeFull();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(toolbar, grid);
    }

    private HorizontalLayout createRowActions(MovieStats stats) {
        var edit = new Button(new Icon(VaadinIcon.EDIT), e -> openEditor(stats));
        edit.addThemeVariants(ButtonVariant.TERTIARY);
        edit.getElement().setAttribute("aria-label", "Edit stats for " + stats.getMovie().getTitle());

        var delete = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(stats));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        delete.getElement().setAttribute("aria-label", "Delete stats for " + stats.getMovie().getTitle());

        var actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(false);
        return actions;
    }

    private void openEditor(MovieStats stats) {
        boolean creating = stats.getId() == null;

        var dialog = new Dialog();
        dialog.setHeaderTitle(creating ? "New movie stats" : "Edit movie stats");
        dialog.setWidth("32rem");

        var movieCombo = new ComboBox<Movie>("Movie");
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setRequiredIndicatorVisible(true);
        if (creating) {
            var available = movieStatsService.moviesWithoutStats();
            movieCombo.setItems(available);
            if (available.isEmpty()) {
                movieCombo.setHelperText("All movies already have stats. Add a movie first.");
            }
        } else {
            movieCombo.setItems(Collections.singletonList(stats.getMovie()));
            movieCombo.setReadOnly(true);
        }

        var runtime = new IntegerField("Runtime (minutes)");
        runtime.setMin(1);
        runtime.setMax(MovieStats.RUNTIME_MAX_MINUTES);
        runtime.setStepButtonsVisible(true);
        runtime.setRequiredIndicatorVisible(true);

        var imdb = new NumberField("IMDB rating");
        imdb.setMin(0.0);
        imdb.setMax(10.0);
        imdb.setStep(0.1);
        imdb.setStepButtonsVisible(true);
        imdb.setRequiredIndicatorVisible(true);

        var budget = new BigDecimalField("Budget");
        budget.setRequiredIndicatorVisible(true);

        var boxOffice = new BigDecimalField("Box office revenue");
        boxOffice.setRequiredIndicatorVisible(true);

        var reviewCount = new IntegerField("Review count");
        reviewCount.setMin(0);
        reviewCount.setStepButtonsVisible(true);
        reviewCount.setRequiredIndicatorVisible(true);

        var form = new FormLayout(movieCombo, runtime, imdb, budget, boxOffice, reviewCount);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("32em", 2));
        form.setColspan(movieCombo, 2);

        var binder = new BeanValidationBinder<>(MovieStats.class);
        binder.forField(movieCombo).asRequired("Movie is required").bind("movie");
        binder.forField(runtime).asRequired("Runtime is required").bind("runtimeMinutes");
        binder.forField(imdb).asRequired("IMDB rating is required").bind("imdbRating");
        binder.forField(budget).asRequired("Budget is required").bind("budget");
        binder.forField(boxOffice).asRequired("Box office revenue is required").bind("boxOfficeRevenue");
        binder.forField(reviewCount).asRequired("Review count is required").bind("reviewCount");
        binder.readBean(stats);

        var save = new Button("Save", e -> {
            try {
                binder.writeBean(stats);
                movieStatsService.save(stats);
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show(creating ? "Stats added" : "Stats updated", 3000,
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

    private void confirmDelete(MovieStats stats) {
        var confirm = new ConfirmDialog();
        confirm.setHeader("Delete stats");
        confirm.setText("Delete stats for \"" + stats.getMovie().getTitle() + "\"? This cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            movieStatsService.delete(stats.getId());
            grid.getDataProvider().refreshAll();
            Notification.show("Stats deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.SUCCESS);
        });
        confirm.open();
    }
}
