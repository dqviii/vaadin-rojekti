package com.tonip.movie.ui;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.ShowtimeService;
import com.tonip.movie.domain.Movie;
import com.tonip.movie.domain.ScreenType;
import com.tonip.movie.domain.Showtime;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "showtimes", layout = MainLayout.class)
@PageTitle("Showtimes")
@Menu(order = 15, icon = "vaadin:ticket", title = "Showtimes")
@PermitAll
public class ShowtimeListView extends VerticalLayout {

    private final ShowtimeService showtimeService;
    private final Grid<Showtime> grid = new Grid<>(Showtime.class, false);

    public ShowtimeListView(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;

        var newBtn = new Button("New showtime", new Icon(VaadinIcon.PLUS), e -> openEditor(new Showtime()));
        newBtn.addThemeVariants(ButtonVariant.PRIMARY);

        var viewTitle = new ViewTitle("Showtimes");
        var toolbar = new HorizontalLayout(viewTitle, newBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(viewTitle);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(getLocale());
        var currencyFormat = NumberFormat.getCurrencyInstance(getLocale());

        grid.addColumn(s -> s.getMovie().getTitle()).setHeader("Movie").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(s -> dateTimeFormatter.format(s.getStartTime())).setHeader("Start").setAutoWidth(true);
        grid.addColumn(Showtime::getTheaterHall).setHeader("Hall").setAutoWidth(true);
        grid.addColumn(s -> s.getScreenType().getDisplayName()).setHeader("Screen").setAutoWidth(true);
        grid.addColumn(s -> currencyFormat.format(s.getTicketPrice())).setHeader("Price").setAutoWidth(true);
        grid.addColumn(Showtime::getAvailableSeats).setHeader("Seats").setAutoWidth(true);
        grid.addComponentColumn(this::createRowActions).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItems(query -> showtimeService.list(toSpringPageRequest(query)).stream());
        grid.setEmptyStateText("No showtimes yet. Click \"New showtime\" to add one.");
        grid.setSizeFull();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(toolbar, grid);
    }

    private HorizontalLayout createRowActions(Showtime showtime) {
        var edit = new Button(new Icon(VaadinIcon.EDIT), e -> openEditor(showtime));
        edit.addThemeVariants(ButtonVariant.TERTIARY);
        edit.getElement().setAttribute("aria-label", "Edit showtime for " + showtime.getMovie().getTitle());

        var delete = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(showtime));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        delete.getElement().setAttribute("aria-label", "Delete showtime for " + showtime.getMovie().getTitle());

        var actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(false);
        return actions;
    }

    private void openEditor(Showtime showtime) {
        boolean creating = showtime.getId() == null;

        var dialog = new Dialog();
        dialog.setHeaderTitle(creating ? "New showtime" : "Edit showtime");
        dialog.setWidth("32rem");

        var movieCombo = new ComboBox<Movie>("Movie");
        movieCombo.setItems(showtimeService.allMovies());
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setRequiredIndicatorVisible(true);

        var startTime = new DateTimePicker("Start time");
        startTime.setStep(java.time.Duration.ofMinutes(5));
        startTime.setRequiredIndicatorVisible(true);

        var theaterHall = new TextField("Theater hall");
        theaterHall.setMaxLength(Showtime.THEATER_HALL_MAX_LENGTH);
        theaterHall.setRequiredIndicatorVisible(true);

        var screenType = new ComboBox<ScreenType>("Screen type");
        screenType.setItems(ScreenType.values());
        screenType.setItemLabelGenerator(ScreenType::getDisplayName);
        screenType.setRequiredIndicatorVisible(true);

        var ticketPrice = new BigDecimalField("Ticket price");
        ticketPrice.setRequiredIndicatorVisible(true);

        var availableSeats = new IntegerField("Available seats");
        availableSeats.setMin(0);
        availableSeats.setMax(Showtime.AVAILABLE_SEATS_MAX);
        availableSeats.setStepButtonsVisible(true);
        availableSeats.setRequiredIndicatorVisible(true);

        var form = new FormLayout(movieCombo, startTime, theaterHall, screenType, ticketPrice, availableSeats);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("32em", 2));
        form.setColspan(movieCombo, 2);
        form.setColspan(startTime, 2);

        var binder = new BeanValidationBinder<>(Showtime.class);
        binder.forField(movieCombo).asRequired("Movie is required").bind("movie");
        binder.forField(startTime).asRequired("Start time is required").bind("startTime");
        binder.forField(theaterHall).asRequired("Theater hall is required").bind("theaterHall");
        binder.forField(screenType).asRequired("Screen type is required").bind("screenType");
        binder.forField(ticketPrice).asRequired("Ticket price is required").bind("ticketPrice");
        binder.forField(availableSeats).asRequired("Available seats is required").bind("availableSeats");
        binder.readBean(showtime);

        var save = new Button("Save", e -> {
            try {
                binder.writeBean(showtime);
                showtimeService.save(showtime);
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show(creating ? "Showtime added" : "Showtime updated", 3000,
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

    private void confirmDelete(Showtime showtime) {
        var confirm = new ConfirmDialog();
        confirm.setHeader("Delete showtime");
        confirm.setText("Delete this showtime for \"" + showtime.getMovie().getTitle() + "\"? This cannot be undone.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            showtimeService.delete(showtime.getId());
            grid.getDataProvider().refreshAll();
            Notification.show("Showtime deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.SUCCESS);
        });
        confirm.open();
    }
}
