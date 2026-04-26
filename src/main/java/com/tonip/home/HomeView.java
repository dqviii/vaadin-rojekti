package com.tonip.home;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@Menu(order = 0, icon = "vaadin:home", title = "Home")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(new ViewTitle("Home"));
        add(new Paragraph("Welcome to the Movie / Cinema Operations app."));
        add(new Paragraph("Sign in to manage movies, showtimes, genres, and screenings."));
    }
}