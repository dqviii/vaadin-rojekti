package com.tonip.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.select.Select;

import java.time.Year;
import java.util.Locale;
import com.tonip.base.MovieI18NProvider;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

@Layout
@AnonymousAllowed
public final class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        addToNavbar(createApplicationHeader());
        addToDrawer(createApplicationDrawer(), createApplicationFooter());
    }

    private Component createApplicationHeader() {
        var drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel("Toggle navigation");

        var appLogo = new Avatar("Movie");
        appLogo.addClassName("app-logo");
        appLogo.addThemeVariants(AvatarVariant.AURA_FILLED, AvatarVariant.XSMALL);

        var appName = new Span("Movie Database Application");
        appName.addClassName("app-name");

        var brand = new HorizontalLayout(appLogo, appName);
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(true);

        var header = new HorizontalLayout(drawerToggle, brand, createLanguageSwitch(), createUserMenu());
        header.addClassName("app-header");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.expand(brand);
        return header;
    }

    private Component createLanguageSwitch() {
        var select = new Select<Locale>();
        select.setItems(Locale.ENGLISH, MovieI18NProvider.FINNISH);
        select.setItemLabelGenerator(l -> l.getLanguage().toUpperCase(Locale.ROOT));
        select.setWidth("80px");
        select.addClassName("app-lang-switch");

        var current = VaadinSession.getCurrent().getLocale();
        select.setValue("fi".equalsIgnoreCase(current != null ? current.getLanguage() : "")
                ? MovieI18NProvider.FINNISH
                : Locale.ENGLISH);

        select.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                return;
            }
            VaadinSession.getCurrent().setLocale(e.getValue());
            UI.getCurrent().getPage().reload();
        });
        return select;
    }

    private Component createUserMenu() {
        var userArea = new HorizontalLayout();
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.setSpacing(true);

        authContext.getPrincipalName().ifPresentOrElse(name -> {
            var userIcon = new Icon(VaadinIcon.USER);
            userIcon.addClassName("app-user-icon");

            var userName = new Span(name);
            userName.addClassName("app-user-name");

            var logout = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT), e -> authContext.logout());
            logout.addThemeVariants(ButtonVariant.TERTIARY);
            logout.addClassName("app-logout");

            userArea.add(userIcon, userName, logout);
        }, () -> {
            var login = new Button("Sign in", new Icon(VaadinIcon.SIGN_IN),
                    e -> getUI().ifPresent(ui -> ui.navigate("login")));
            login.addThemeVariants(ButtonVariant.TERTIARY);
            userArea.add(login);
        });

        return userArea;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private Component createApplicationFooter() {
        var copyright = new Span("© " + Year.now().getValue());
        copyright.addClassName("app-footer-copy");

        var author = new Span("Toni Piispa");
        author.addClassName("app-footer-author");

        var topLine = new HorizontalLayout(copyright, author);
        topLine.setAlignItems(FlexComponent.Alignment.CENTER);
        topLine.setSpacing(true);

        var sourceLink = new Anchor("https://github.com/dqviii/vaadin-rojekti", "Source on GitHub ↗");
        sourceLink.setTarget(AnchorTarget.BLANK);
        sourceLink.getElement().setAttribute("rel", "noopener noreferrer");
        sourceLink.addClassName("app-footer-link");

        var courseNote = new Span("Vaadin coursework");
        courseNote.addClassName("app-footer-note");

        var footer = new VerticalLayout(topLine, sourceLink, courseNote);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setSpacing(false);
        footer.setPadding(true);
        footer.addClassName("app-footer");
        return footer;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            Component icon = null;
            if (menuEntry.icon().contains(".svg")) {
                icon = new SvgIcon(menuEntry.icon());
            } else {
                icon = new Icon(menuEntry.icon());
            }
            return new SideNavItem(menuEntry.title(), menuEntry.path(), icon);
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
