package com.tonip.movie.ui;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.tonip.movie.GenreService;
import com.tonip.movie.domain.Genre;
import com.tonip.movie.domain.TargetAudience;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "genres", layout = MainLayout.class)
@PageTitle("Genres")
@Menu(order = 20, icon = "vaadin:tags", title = "Genres")
@PermitAll
public class GenreListView extends VerticalLayout {

    private final GenreService genreService;
    private final Grid<Genre> grid = new Grid<>(Genre.class, false);

    public GenreListView(GenreService genreService) {
        this.genreService = genreService;

        var newBtn = new Button("New genre", new Icon(VaadinIcon.PLUS), e -> openEditor(new Genre()));
        newBtn.addThemeVariants(ButtonVariant.PRIMARY);

        var viewTitle = new ViewTitle("Genres");
        var toolbar = new HorizontalLayout(viewTitle, newBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(viewTitle);

        grid.addColumn(Genre::getGenreName).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Genre::getDescription).setHeader("Description").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Genre::getIconCode).setHeader("Icon code").setAutoWidth(true);
        grid.addColumn(g -> Boolean.TRUE.equals(g.getMainstream()) ? "Yes" : "No")
                .setHeader("Mainstream").setAutoWidth(true);
        grid.addColumn(g -> g.getTargetAudience().getDisplayName()).setHeader("Audience").setAutoWidth(true);
        grid.addComponentColumn(this::createRowActions).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);
        grid.setItems(query -> genreService.list(toSpringPageRequest(query)).stream());
        grid.setEmptyStateText("No genres yet. Click \"New genre\" to add one.");
        grid.setSizeFull();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(toolbar, grid);
    }

    private HorizontalLayout createRowActions(Genre genre) {
        var edit = new Button(new Icon(VaadinIcon.EDIT), e -> openEditor(genre));
        edit.addThemeVariants(ButtonVariant.TERTIARY);
        edit.getElement().setAttribute("aria-label", "Edit " + genre.getGenreName());

        var delete = new Button(new Icon(VaadinIcon.TRASH), e -> confirmDelete(genre));
        delete.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        delete.getElement().setAttribute("aria-label", "Delete " + genre.getGenreName());

        var actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(false);
        return actions;
    }

    private void openEditor(Genre genre) {
        boolean creating = genre.getId() == null;

        var dialog = new Dialog();
        dialog.setHeaderTitle(creating ? "New genre" : "Edit genre");
        dialog.setWidth("32rem");

        var name = new TextField("Name");
        name.setMaxLength(Genre.GENRE_NAME_MAX_LENGTH);
        name.setRequiredIndicatorVisible(true);

        var description = new TextField("Description");
        description.setMaxLength(Genre.DESCRIPTION_MAX_LENGTH);
        description.setRequiredIndicatorVisible(true);

        var iconCode = new TextField("Icon code");
        iconCode.setMaxLength(Genre.ICON_CODE_MAX_LENGTH);
        iconCode.setHelperText("e.g. vaadin:film");
        iconCode.setRequiredIndicatorVisible(true);

        var mainstream = new Checkbox("Mainstream");

        var targetAudience = new ComboBox<TargetAudience>("Target audience");
        targetAudience.setItems(TargetAudience.values());
        targetAudience.setItemLabelGenerator(TargetAudience::getDisplayName);
        targetAudience.setRequiredIndicatorVisible(true);

        var form = new FormLayout(name, description, iconCode, mainstream, targetAudience);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("32em", 2));
        form.setColspan(description, 2);

        var binder = new BeanValidationBinder<>(Genre.class);
        binder.forField(name).asRequired("Name is required").bind("genreName");
        binder.forField(description).asRequired("Description is required").bind("description");
        binder.forField(iconCode).asRequired("Icon code is required").bind("iconCode");
        binder.forField(mainstream).bind("mainstream");
        binder.forField(targetAudience).asRequired("Target audience is required").bind("targetAudience");
        binder.readBean(genre);

        var save = new Button("Save", e -> {
            try {
                binder.writeBean(genre);
                genreService.save(genre);
                grid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show(creating ? "Genre added" : "Genre updated", 3000,
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

    private void confirmDelete(Genre genre) {
        var confirm = new ConfirmDialog();
        confirm.setHeader("Delete genre");
        confirm.setText("Delete \"" + genre.getGenreName()
                + "\"? It will be removed from any movies that use it.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Delete");
        confirm.setConfirmButtonTheme("error primary");
        confirm.addConfirmListener(e -> {
            genreService.delete(genre.getId());
            grid.getDataProvider().refreshAll();
            Notification.show("Genre deleted", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.SUCCESS);
        });
        confirm.open();
    }
}
