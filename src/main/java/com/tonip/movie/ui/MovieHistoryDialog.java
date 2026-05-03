package com.tonip.movie.ui;

import com.tonip.movie.domain.Movie;
import com.tonip.movie.history.MovieHistoryService;
import com.tonip.movie.history.MovieRevision;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

public class MovieHistoryDialog extends Dialog {

    public MovieHistoryDialog(Movie movie, MovieHistoryService historyService, Locale locale) {
        setHeaderTitle("History — " + movie.getTitle());
        setWidth("60rem");

        List<MovieRevision> revisions = historyService.findHistory(movie.getId());

        if (revisions.isEmpty()) {
            add(new Paragraph("No revisions recorded for this movie yet."));
        } else {
            var formatter = DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    .withLocale(locale)
                    .withZone(ZoneId.systemDefault());

            Grid<MovieRevision> grid = new Grid<>();
            grid.addColumn(MovieRevision::revisionNumber).setHeader("Rev").setAutoWidth(true).setFlexGrow(0);
            grid.addColumn(MovieRevision::typeLabel).setHeader("Change").setAutoWidth(true).setFlexGrow(0);
            grid.addColumn(r -> formatter.format(r.revisionDate())).setHeader("When").setAutoWidth(true);
            grid.addColumn(r -> nullSafe(r.title())).setHeader("Title").setAutoWidth(true).setFlexGrow(1);
            grid.addColumn(r -> nullSafe(r.directorName())).setHeader("Director").setAutoWidth(true);
            grid.addColumn(r -> nullSafe(r.updatedBy())).setHeader("By").setAutoWidth(true);
            grid.setItems(revisions);
            grid.setHeight("420px");
            add(grid);
        }

        var close = new Button("Close", e -> close());
        getFooter().add(close);
    }

    private static String nullSafe(String s) {
        return s == null ? "—" : s;
    }
}
