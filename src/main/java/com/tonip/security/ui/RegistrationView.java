package com.tonip.security.ui;

import com.tonip.security.UserRegistrationService;
import com.tonip.security.UserRegistrationService.UsernameAlreadyExistsException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "register", autoLayout = false)
@PageTitle("Create account")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private static final int USERNAME_MIN = 3;
    private static final int USERNAME_MAX = 50;

    private final UserRegistrationService registrationService;

    private final TextField usernameField = new TextField("Username");
    private final PasswordField passwordField = new PasswordField("Password");
    private final PasswordField confirmField = new PasswordField("Confirm password");

    public RegistrationView(UserRegistrationService registrationService) {
        this.registrationService = registrationService;

        addClassName("registration-view");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setMinLength(USERNAME_MIN);
        usernameField.setMaxLength(USERNAME_MAX);
        usernameField.setHelperText(USERNAME_MIN + "–" + USERNAME_MAX + " characters");

        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setMinLength(UserRegistrationService.PASSWORD_MIN_LENGTH);
        passwordField.setMaxLength(UserRegistrationService.PASSWORD_MAX_LENGTH);
        passwordField.setHelperText("At least " + UserRegistrationService.PASSWORD_MIN_LENGTH + " characters");

        confirmField.setRequiredIndicatorVisible(true);

        var submit = new Button("Create account", e -> submit());
        submit.addThemeVariants(ButtonVariant.PRIMARY);

        var form = new FormLayout(usernameField, passwordField, confirmField, submit);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        form.setMaxWidth("28rem");
        form.setColspan(submit, 1);

        var loginLink = new Anchor("login", "Already have an account? Sign in");

        var card = new VerticalLayout(new H1("Create account"),
                new Paragraph("Register a new account to manage movies."),
                form, loginLink);
        card.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.setMaxWidth("28rem");
        card.setPadding(true);
        card.setSpacing(true);

        add(card);
    }

    private void submit() {
        var username = usernameField.getValue() == null ? "" : usernameField.getValue().trim();
        var password = passwordField.getValue() == null ? "" : passwordField.getValue();
        var confirm = confirmField.getValue() == null ? "" : confirmField.getValue();

        clearErrors();
        boolean valid = true;

        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            usernameField.setInvalid(true);
            usernameField.setErrorMessage("Username must be " + USERNAME_MIN + "–" + USERNAME_MAX + " characters");
            valid = false;
        }
        if (password.length() < UserRegistrationService.PASSWORD_MIN_LENGTH) {
            passwordField.setInvalid(true);
            passwordField.setErrorMessage("Password must be at least " + UserRegistrationService.PASSWORD_MIN_LENGTH + " characters");
            valid = false;
        }
        if (!password.equals(confirm)) {
            confirmField.setInvalid(true);
            confirmField.setErrorMessage("Passwords do not match");
            valid = false;
        }
        if (!valid) {
            return;
        }

        try {
            registrationService.register(username, password);
            Notification.show("Account created. You can now sign in.", 4000,
                    Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (UsernameAlreadyExistsException ex) {
            usernameField.setInvalid(true);
            usernameField.setErrorMessage("That username is already taken");
        }
    }

    private void clearErrors() {
        usernameField.setInvalid(false);
        passwordField.setInvalid(false);
        confirmField.setInvalid(false);
    }
}
