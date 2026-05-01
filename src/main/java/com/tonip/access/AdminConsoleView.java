package com.tonip.access;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "admin/console", layout = MainLayout.class)
@PageTitle("Admin console")
@Menu(order = 91, icon = "vaadin:cog", title = "Admin console")
@RolesAllowed("ADMIN")
public class AdminConsoleView extends VerticalLayout {

    public AdminConsoleView() {
        setPadding(true);
        setSpacing(true);
        add(new ViewTitle("Admin console"));
        add(new Paragraph(
                "This is a demo route. Only users with role ADMIN can access it. "
                        + "SUPER and USER accounts are redirected to the access-denied page."));
    }
}
