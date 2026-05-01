package com.tonip.access;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Access denied")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout
        implements HasErrorParameter<AccessDeniedException> {

    public AccessDeniedView() {
        addClassName("access-denied-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        add(new H1("Access denied"));
        add(new Paragraph(
                "Your account does not have permission to view this page. "
                        + "Sign in with a different role, or return to the home page."));

        var goHome = new Button("Back to home",
                e -> getUI().ifPresent(ui -> ui.navigate("")));
        goHome.addThemeVariants(ButtonVariant.PRIMARY);
        add(goHome);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<AccessDeniedException> parameter) {
        return HttpStatusCode.FORBIDDEN.getCode();
    }
}
