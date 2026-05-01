package com.tonip.access;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lounge", layout = MainLayout.class)
@PageTitle("Member lounge")
@Menu(order = 90, icon = "vaadin:coffee", title = "Member lounge")
@RolesAllowed({"SUPER", "USER"})
public class MemberLoungeView extends VerticalLayout {

    public MemberLoungeView() {
        setPadding(true);
        setSpacing(true);
        add(new ViewTitle("Member lounge"));
        add(new Paragraph(
                "This is a demo route. Only users with role SUPER or USER can access it. "
                        + "ADMIN accounts are redirected to the access-denied page."));
    }
}
