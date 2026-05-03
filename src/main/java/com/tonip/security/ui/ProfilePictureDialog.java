package com.tonip.security.ui;

import com.tonip.security.UserProfileService;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ProfilePictureDialog extends Dialog {

    private final UserProfileService profileService;
    private final String username;
    private final Runnable onSaved;

    private byte[] pendingBytes;
    private String pendingMimeType;
    private final Avatar preview;

    public ProfilePictureDialog(UserProfileService profileService,
                                String username,
                                byte[] currentPicture,
                                String currentMimeType,
                                Runnable onSaved) {
        this.profileService = profileService;
        this.username = username;
        this.onSaved = onSaved;

        setHeaderTitle("Profile picture");
        setWidth("24rem");

        preview = new Avatar(username);
        preview.setHeight("96px");
        preview.setWidth("96px");
        if (currentPicture != null && currentPicture.length > 0) {
            applyPreview(currentPicture, currentMimeType);
        }

        var hint = new Paragraph(
                "Upload a JPEG or PNG image, max 5 MB. The picture replaces your header avatar.");
        hint.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)");

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) UserProfileService.MAX_PROFILE_PICTURE_BYTES);
        upload.setDropAllowed(true);
        upload.addSucceededListener(e -> {
            try {
                pendingBytes = buffer.getInputStream().readAllBytes();
                pendingMimeType = e.getMIMEType();
                applyPreview(pendingBytes, pendingMimeType);
            } catch (IOException ex) {
                Notification.show("Could not read uploaded file", 3000,
                        Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.ERROR);
            }
        });

        var content = new VerticalLayout(preview, hint, upload);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(false);
        add(content);

        var save = new Button("Save", e -> save());
        save.addThemeVariants(ButtonVariant.PRIMARY);

        var remove = new Button("Remove", e -> remove());
        remove.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.ERROR);
        remove.setEnabled(currentPicture != null && currentPicture.length > 0);

        var cancel = new Button("Cancel", e -> close());

        getFooter().add(remove, cancel, save);
    }

    private void applyPreview(byte[] bytes, String mimeType) {
        String safeMime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";
        var resource = new StreamResource("avatar",
                () -> new ByteArrayInputStream(bytes));
        resource.setContentType(safeMime);
        preview.setImageResource(resource);
    }

    private void save() {
        if (pendingBytes == null) {
            close();
            return;
        }
        profileService.updateProfilePicture(username, pendingBytes, pendingMimeType);
        Notification.show("Profile picture updated", 2500,
                Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.SUCCESS);
        onSaved.run();
        close();
    }

    private void remove() {
        profileService.removeProfilePicture(username);
        Notification.show("Profile picture removed", 2500,
                Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.SUCCESS);
        onSaved.run();
        close();
    }
}
