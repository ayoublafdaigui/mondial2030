package com.mondial2030.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Manages internationalization (I18n) for the JavaFX application.
 * Provides methods to switch locales and reload FXML bundles at runtime.
 */
public class LanguageManager {

    private static LanguageManager instance;
    
    // Observable locale property for binding
    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>();
    
    // Supported locales
    private static final Map<String, Locale> SUPPORTED_LOCALES = new LinkedHashMap<>();
    
    // Resource bundle base name
    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    
    // Current resource bundle
    private ResourceBundle resourceBundle;
    
    // Listeners for locale changes
    private final List<LocaleChangeListener> listeners = new ArrayList<>();

    static {
        // Initialize supported locales
        SUPPORTED_LOCALES.put("English", Locale.ENGLISH);
        SUPPORTED_LOCALES.put("العربية", new Locale("ar"));
        SUPPORTED_LOCALES.put("Français", Locale.FRENCH);
        SUPPORTED_LOCALES.put("Español", new Locale("es"));
    }

    private LanguageManager() {
        // Default to English
        setLocale(Locale.ENGLISH);
    }

    /**
     * Gets the singleton instance of LanguageManager.
     */
    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    /**
     * Sets the current locale and loads the corresponding resource bundle.
     *
     * @param locale The locale to set
     */
    public void setLocale(Locale locale) {
        try {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
            currentLocale.set(locale);
            Locale.setDefault(locale);
            
            // Notify all listeners
            notifyListeners();
            
            System.out.println("Language changed to: " + locale.getDisplayLanguage());
        } catch (MissingResourceException e) {
            System.err.println("Resource bundle not found for locale: " + locale);
            // Fallback to English
            if (!locale.equals(Locale.ENGLISH)) {
                setLocale(Locale.ENGLISH);
            }
        }
    }

    /**
     * Sets the locale by language code (e.g., "en", "ar", "fr", "es").
     *
     * @param languageCode The ISO language code
     */
    public void setLocale(String languageCode) {
        setLocale(new Locale(languageCode));
    }

    /**
     * Gets the current locale.
     */
    public Locale getCurrentLocale() {
        return currentLocale.get();
    }

    /**
     * Gets the current locale property for binding.
     */
    public ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    /**
     * Gets a localized string by key.
     *
     * @param key The message key
     * @return The localized string, or the key if not found
     */
    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Missing translation for key: " + key);
            return key;
        }
    }

    /**
     * Gets a localized string with parameter substitution.
     *
     * @param key  The message key
     * @param args Arguments for parameter substitution
     * @return The formatted localized string
     */
    public String getString(String key, Object... args) {
        try {
            String pattern = resourceBundle.getString(key);
            return java.text.MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            System.err.println("Missing translation for key: " + key);
            return key;
        }
    }

    /**
     * Gets the current resource bundle.
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Gets all supported locales.
     */
    public Map<String, Locale> getSupportedLocales() {
        return Collections.unmodifiableMap(SUPPORTED_LOCALES);
    }

    /**
     * Gets the list of supported locale names for display.
     */
    public List<String> getSupportedLocaleNames() {
        return new ArrayList<>(SUPPORTED_LOCALES.keySet());
    }

    /**
     * Loads an FXML file with the current resource bundle.
     *
     * @param fxmlPath Path to the FXML file (relative to resources)
     * @return The loaded Parent node
     * @throws IOException if loading fails
     */
    public Parent loadFXML(String fxmlPath) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        
        FXMLLoader loader = new FXMLLoader(fxmlUrl, resourceBundle);
        return loader.load();
    }

    /**
     * Creates an FXMLLoader configured with the current resource bundle.
     *
     * @param fxmlPath Path to the FXML file
     * @return Configured FXMLLoader
     */
    public FXMLLoader createLoader(String fxmlPath) {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        return new FXMLLoader(fxmlUrl, resourceBundle);
    }

    /**
     * Checks if the current locale is RTL (Right-to-Left).
     */
    public boolean isRTL() {
        String lang = currentLocale.get().getLanguage();
        return "ar".equals(lang) || "he".equals(lang) || "fa".equals(lang);
    }

    /**
     * Gets the appropriate node orientation for the current locale.
     */
    public javafx.geometry.NodeOrientation getNodeOrientation() {
        return isRTL() ? javafx.geometry.NodeOrientation.RIGHT_TO_LEFT 
                       : javafx.geometry.NodeOrientation.LEFT_TO_RIGHT;
    }

    /**
     * Adds a listener to be notified when the locale changes.
     */
    public void addLocaleChangeListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a locale change listener.
     */
    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (LocaleChangeListener listener : listeners) {
            listener.onLocaleChanged(currentLocale.get());
        }
    }

    /**
     * Listener interface for locale change events.
     */
    @FunctionalInterface
    public interface LocaleChangeListener {
        void onLocaleChanged(Locale newLocale);
    }

    /**
     * Switches to the next available language (cycles through supported locales).
     */
    public void switchToNextLanguage() {
        List<Locale> locales = new ArrayList<>(SUPPORTED_LOCALES.values());
        int currentIndex = locales.indexOf(currentLocale.get());
        int nextIndex = (currentIndex + 1) % locales.size();
        setLocale(locales.get(nextIndex));
    }

    /**
     * Reloads the current view with the new locale.
     * This is a helper method to refresh FXML content.
     *
     * @param fxmlPath    The FXML file path
     * @param container   The container to update
     * @param <T>         The container type
     * @return The newly loaded content
     * @throws IOException if loading fails
     */
    public <T extends javafx.scene.layout.Pane> T reloadView(String fxmlPath, T container) 
            throws IOException {
        container.getChildren().clear();
        Parent newContent = loadFXML(fxmlPath);
        container.getChildren().add(newContent);
        container.setNodeOrientation(getNodeOrientation());
        return container;
    }
}
