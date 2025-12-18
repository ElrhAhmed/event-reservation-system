package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.combobox.ComboBox;
import com.vaadin.flow. component.html.H2;
import com.vaadin. flow.component.html. Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma. projet.events.service.EventService;
import ma.projet.events.ui.component.EventCard;
import ma. projet.events.ui.component. EventSearchBar;
import ma. projet.events.ui.layout.PublicLayout;

import java.util. List;

/**
 * Vue liste de tous les événements avec filtres
 * Route : /events
 */
@Route(value = "events", layout = PublicLayout. class)
@PageTitle("Événements - Festivent")
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final HorizontalLayout eventsContainer;

    // Composants de filtres
    private EventSearchBar searchBar;
    private ComboBox<EventCategory> categoryFilter;
    private NumberField minPriceFilter;
    private NumberField maxPriceFilter;

    public EventListView(EventService eventService) {
        this.eventService = eventService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background-color", "var(--festivent-bg)");

        // Titre de la page
        H2 title = new H2("Tous les événements");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        // Barre de filtres
        HorizontalLayout filters = createFilters();

        // Container des événements (grille)
        eventsContainer = new HorizontalLayout();
        eventsContainer.setWidthFull();
        eventsContainer. setSpacing(true);
        eventsContainer.getStyle()
                .set("flex-wrap", "wrap")
                .set("justify-content", "flex-start");

        add(title, filters, eventsContainer);

        // Charger tous les événements au démarrage
        loadAllEvents();
    }

    /**
     * Crée la barre de filtres
     */
    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.setAlignItems(Alignment.END);
        filters.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--festivent-shadow-sm)");

        // Filtre :  Recherche par mot-clé
        searchBar = new EventSearchBar("Rechercher par titre ou description...");
        searchBar. setWidth("350px");
        searchBar.addValueChangeListener(e -> applyFilters());

        // Filtre : Catégorie
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(category ->
                category.getIcon() + " " + category.getLabel()
        );
        categoryFilter.setPlaceholder("Toutes");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        // Filtre : Prix minimum
        minPriceFilter = new NumberField("Prix min (DH)");
        minPriceFilter.setMin(0);
        minPriceFilter.setStep(50);
        minPriceFilter.setPlaceholder("0");
        minPriceFilter.setClearButtonVisible(true);
        minPriceFilter.setWidth("150px");
        minPriceFilter.addValueChangeListener(e -> applyFilters());

        // Filtre : Prix maximum
        maxPriceFilter = new NumberField("Prix max (DH)");
        maxPriceFilter.setMin(0);
        maxPriceFilter.setStep(50);
        maxPriceFilter.setPlaceholder("∞");
        maxPriceFilter.setClearButtonVisible(true);
        maxPriceFilter.setWidth("150px");
        maxPriceFilter.addValueChangeListener(e -> applyFilters());

        filters.add(searchBar, categoryFilter, minPriceFilter, maxPriceFilter);

        // Permettre au searchBar de prendre plus de place
        filters.expand(searchBar);

        return filters;
    }

    /**
     * Applique les filtres sélectionnés
     */
    private void applyFilters() {
        String keyword = searchBar.getValue();
        EventCategory category = categoryFilter.getValue();
        Double minPrice = minPriceFilter.getValue();
        Double maxPrice = maxPriceFilter.getValue();

        try {
            List<Event> events;

            // Si recherche par mot-clé, utiliser la méthode search()
            if (keyword != null && !keyword.isBlank()) {
                events = eventService.searchEvents(keyword);

                // Appliquer les autres filtres manuellement sur les résultats
                events = events.stream()
                        .filter(e -> category == null || e.getCategorie().equals(category))
                        .filter(e -> minPrice == null || e.getPrixUnitaire() >= minPrice)
                        .filter(e -> maxPrice == null || e.getPrixUnitaire() <= maxPrice)
                        .toList();
            } else {
                // Sinon, utiliser la méthode searchWithFilters()
                events = eventService.searchWithFilters(
                        category,
                        null,  // ville
                        null,  // dateMin
                        null,  // dateMax
                        null,  // lieu
                        minPrice,
                        maxPrice
                );
            }

            displayEvents(events);

        } catch (Exception e) {
            showError("Erreur lors de l'application des filtres");
            e.printStackTrace();
        }
    }

    /**
     * Charge tous les événements disponibles (publiés)
     */
    private void loadAllEvents() {
        try {
            // Récupérer tous les événements publiés
            List<Event> events = eventService.searchWithFilters(
                    null, null, null, null, null, null, null
            );
            displayEvents(events);

        } catch (Exception e) {
            showError("Erreur lors du chargement des événements");
            e.printStackTrace();
        }
    }

    /**
     * Affiche la liste des événements dans le container
     */
    private void displayEvents(List<Event> events) {
        eventsContainer.removeAll();

        if (events == null || events.isEmpty()) {
            // Aucun événement trouvé
            VerticalLayout emptyState = createEmptyState();
            eventsContainer.add(emptyState);
        } else {
            // Afficher chaque événement
            for (Event event : events) {
                EventCard card = new EventCard(event);

                // Click sur la card → Navigation vers les détails
                card.addClickListener(clickEvent -> {
                    UI. getCurrent().navigate("event/" + event.getId());
                });

                eventsContainer. add(card);
            }

            // Afficher le nombre de résultats
            updateResultCount(events.size());
        }
    }

    /**
     * Crée un état vide (aucun événement trouvé)
     */
    private VerticalLayout createEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setWidthFull();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setPadding(true);
        emptyState.setSpacing(true);

        Paragraph message = new Paragraph("Aucun événement trouvé");
        message.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("color", "var(--festivent-text-secondary)")
                .set("text-align", "center");

        Paragraph hint = new Paragraph("Essayez de modifier vos critères de recherche");
        hint.getStyle()
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-align", "center");

        emptyState.add(message, hint);
        return emptyState;
    }

    /**
     * Met à jour le compteur de résultats dans le titre
     */
    private void updateResultCount(int count) {
        // Récupérer le composant titre (premier enfant)
        if (getComponentCount() > 0 && getComponentAt(0) instanceof H2 title) {
            title.setText("Tous les événements (" + count + ")");
        }
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        eventsContainer.removeAll();

        Paragraph error = new Paragraph(message);
        error.getStyle()
                .set("color", "var(--festivent-error)")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("padding", "var(--lumo-space-xl)")
                .set("text-align", "center");

        eventsContainer.add(error);
    }
}