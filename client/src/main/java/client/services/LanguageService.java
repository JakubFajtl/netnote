package client.services;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static client.Main.DEFAULT_LOCALE;

public class LanguageService {

    private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(DEFAULT_LOCALE);

    public void setLocale(Locale newLocale) {
        locale.set(newLocale);
    }

    public void bindMenuItem(MenuItem menuItem, String key){
        menuItem.textProperty().bind(Bindings.createStringBinding(
                () -> ResourceBundle.getBundle("Items", locale.get()).getString(key),
                locale
        ));
    }

    public void bindLabeled(Labeled labeled, String key){
        labeled.textProperty().bind(Bindings.createStringBinding(
                () -> ResourceBundle.getBundle("Items", locale.get()).getString(key),
                locale
        ));
    }

    public void bindLabeledTooltip(Labeled labeled, String key) {
        labeled.tooltipProperty().bind(Bindings.createObjectBinding(
                () -> {
                    String tooltipText = ResourceBundle.getBundle(
                            "Items", locale.get()).getString(key);
                    return new Tooltip(tooltipText);
                },
                locale
        ));
    }

    public void bindTextInputControl(TextInputControl textInputControl, String key){
        textInputControl.promptTextProperty().bind(Bindings.createStringBinding(
                () -> ResourceBundle.getBundle("Items", locale.get()).getString(key),
                locale
        ));
    }

    public void bindTooltip(Tooltip tooltip, String key){
        tooltip.textProperty().bind(Bindings.createStringBinding(
                () -> ResourceBundle.getBundle("Items", locale.get()).getString(key),
                locale
        ));
    }

    public String getDescriptionByKey(String key){
        var rb = ResourceBundle.getBundle("Items", getLocale());
        String result;
        try {
            result =  rb.getString(key);
        } catch (MissingResourceException e) {
            result = key;
        }
        return  result;
    }

    public Locale getLocale(){
        return locale.get();
    }
}
