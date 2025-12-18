package ma.projet.events.ui.component;

import com.vaadin.flow. component.textfield.TextField;
import com.vaadin. flow.component.icon.VaadinIcon;
import com. vaadin.flow.data.value.ValueChangeMode;

/**
 * Barre de recherche d'événements
 * Avec icône de recherche et debounce pour optimiser les requêtes
 *
 * Exemple d'utilisation :
 * EventSearchBar searchBar = new EventSearchBar();
 * searchBar.addValueChangeListener(e -> {
 *     String keyword = e.getValue();
 *     searchEvents(keyword);
 * });
 */
public class EventSearchBar extends TextField {

    public EventSearchBar() {
        super();
        configure();
    }

    public EventSearchBar(String placeholder) {
        super();
        setPlaceholder(placeholder);
        configure();
    }

    private void configure() {
        // Configuration de base
        setPlaceholder("Rechercher un événement...");
        setPrefixComponent(VaadinIcon.SEARCH.create());
        setClearButtonVisible(true);
        setWidthFull();

        // Debounce :  attend 300ms après la dernière frappe avant de déclencher l'événement
        setValueChangeMode(ValueChangeMode. LAZY);
        setValueChangeTimeout(300);

        // Style
        getStyle()
                .set("max-width", "600px")
                .set("--vaadin-input-field-border-radius", "var(--lumo-border-radius-l)");
    }

    /**
     * Définit le placeholder
     */
    public EventSearchBar withPlaceholder(String placeholder) {
        setPlaceholder(placeholder);
        return this;
    }

    /**
     * Définit le délai de debounce (en ms)
     */
    public EventSearchBar withDebounce(int timeoutMs) {
        setValueChangeTimeout(timeoutMs);
        return this;
    }
}