package com.tonip.base.ui;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Vaadin wrapper around the Quill rich text editor (external JS library).
 * The {@code value} property holds Quill's HTML output and is synced via
 * the {@code value-changed} event the JS shim dispatches.
 */
@Tag("quill-editor")
@JsModule("./quill-editor.js")
@NpmPackage(value = "quill", version = "2.0.2")
public class QuillEditor extends AbstractSinglePropertyField<QuillEditor, String>
        implements HasSize {

    public QuillEditor() {
        super("value", "", false);
    }

    public QuillEditor(String placeholder) {
        this();
        setPlaceholder(placeholder);
    }

    public void setPlaceholder(String placeholder) {
        if (placeholder == null) {
            getElement().removeAttribute("placeholder");
        } else {
            getElement().setAttribute("placeholder", placeholder);
        }
    }
}
