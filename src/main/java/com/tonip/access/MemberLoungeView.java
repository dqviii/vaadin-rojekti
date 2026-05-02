package com.tonip.access;

import com.tonip.base.ui.MainLayout;
import com.tonip.base.ui.ViewTitle;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lounge", layout = MainLayout.class)
@PageTitle("Member lounge")
@Menu(order = 90, icon = "vaadin:coffee", title = "Member lounge")
@RolesAllowed({"SUPER", "USER"})
public class MemberLoungeView extends VerticalLayout {

    public MemberLoungeView() {
        // C4 evidence: 6 distinct Lumo Utility class categories on a single view
        // (Padding, Margin, Gap, AlignItems, FontSize, TextColor).
        addClassNames(
                LumoUtility.Padding.LARGE,
                LumoUtility.Margin.Top.MEDIUM,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.AlignItems.CENTER);

        var title = new ViewTitle("Member lounge");
        title.addClassName(LumoUtility.FontSize.XXLARGE);

        var body = new Paragraph(
                "This is a demo route. Only users with role SUPER or USER can access it. "
                        + "ADMIN accounts are redirected to the access-denied page.");
        body.addClassName(LumoUtility.TextColor.SECONDARY);

        add(title, body);
    }
}
