package com.tonip.base;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class MovieI18NProvider implements I18NProvider {

    public static final Locale FINNISH = Locale.of("fi");
    private static final List<Locale> LOCALES = List.of(Locale.ENGLISH, FINNISH);
    private static final String BUNDLE = "messages";

    @Override
    public List<Locale> getProvidedLocales() {
        return LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            return "";
        }
        Locale target = (locale != null) ? locale : Locale.ENGLISH;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, target);
            String value = bundle.getString(key);
            return params.length == 0 ? value : MessageFormat.format(value, params);
        } catch (MissingResourceException e) {
            return "!" + key;
        }
    }
}
