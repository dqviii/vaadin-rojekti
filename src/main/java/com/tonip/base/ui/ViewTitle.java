package com.tonip.base.ui;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@StyleSheet("view-title.css")
public class ViewTitle extends Composite<HorizontalLayout> {

    public ViewTitle(String title) {
        addClassName("view-title");
        getContent().add(new H1(title));
    }
}
