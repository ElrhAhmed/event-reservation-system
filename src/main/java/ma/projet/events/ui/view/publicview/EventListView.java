package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.service.EventService;
import ma.projet.events.ui.component.card.EventCard;
import ma.projet.events.ui.component.filter.EventFilterBar;
import ma.projet.events.ui.layout.PublicLayout;
import ma.projet.events.ui.navigation.NavigationManager;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "events", layout = PublicLayout.class)
@PageTitle("Catalogue Événements | FESTIVENT")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final NavigationManager navigationManager;

    // Composants UI
    private final Div eventsGrid;
    private final Span resultsCount;
    private final ComboBox<String> sortSelect;
    private final EventFilterBar filterBar;

    // État local pour le filtrage/tri
    private List<Event> currentResults;

    public EventListView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        // Configuration de la vue
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);
        // Force la hauteur min pour pousser le footer
        setMinHeight("100vh");

        // Conteneur centré pour limiter la largeur sur grands écrans
        setMaxWidth("1240px");
        addClassName(LumoUtility.Margin.Horizontal.AUTO);

        /* ====================================
           1. EN-TÊTE & TRI
           ==================================== */
        H2 title = new H2("Tous les événements");
        title.addClassNames(LumoUtility.Margin.NONE);

        resultsCount = new Span("Chargement...");
        resultsCount.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        VerticalLayout titleBlock = new VerticalLayout(title, resultsCount);
        titleBlock.setPadding(false);
        titleBlock.setSpacing(false);

        // Sélecteur de Tri
        sortSelect = new ComboBox<>("Trier par");
        sortSelect.setItems("Date (Proche -> Loin)", "Prix (Croissant)", "Prix (Décroissant)", "Nom (A-Z)");
        sortSelect.setValue("Date (Proche -> Loin)");
        sortSelect.addValueChangeListener(e -> updateGridDisplay()); // Retrier sans recharger la DB
        sortSelect.setWidth("220px");

        HorizontalLayout headerRow = new HorizontalLayout(titleBlock, sortSelect);
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        // Responsive : Wrap sur mobile pour que le tri passe dessous
        headerRow.addClassName(LumoUtility.FlexWrap.WRAP); 

        /* ====================================
           2. BARRE DE FILTRES
           ==================================== */
        // On réutilise le composant fourni
        filterBar = new EventFilterBar(
                Arrays.asList(EventCategory.values()),
                this::performSearch // Callback quand on clique sur "Filtrer"
        );

        // Petit ajustement CSS pour que le layout horizontal wrap sur mobile
        filterBar.addClassName(LumoUtility.FlexWrap.WRAP);

        /* ====================================
           3. GRILLE DE RÉSULTATS
           ==================================== */
        eventsGrid = new Div();
        eventsGrid.setWidthFull();
        eventsGrid.addClassName(LumoUtility.Margin.Top.LARGE);
        // CSS Grid Responsive
        eventsGrid.getStyle().set("display", "grid");
        eventsGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(300px, 1fr))");
        eventsGrid.getStyle().set("gap", "2rem");

        /* ====================================
           4. ASSEMBLAGE
           ==================================== */
        add(headerRow, filterBar, eventsGrid);

        // Chargement initial (Filtre vide)
        performSearch(new EventFilterBar.EventFilter());
    }

    /**
     * Exécute la recherche via le Service
     */
    private void performSearch(EventFilterBar.EventFilter filter) {
        // 1. Conversion des dates (LocalDate -> LocalDateTime)
        LocalDateTime start = (filter.dateMin != null) ? filter.dateMin.atStartOfDay() : null;
        LocalDateTime end = (filter.dateMax != null) ? filter.dateMax.atTime(LocalTime.MAX) : null;

        // 2. Appel Service
        currentResults = eventService.searchWithFilters(
                filter.categorie,
                (filter.ville != null && !filter.ville.isBlank()) ? filter.ville : null,
                start,
                end,
                (filter.keyword != null && !filter.keyword.isBlank()) ? filter.keyword : null,
                filter.prixMin,
                filter.prixMax
        );

        // 3. Mise à jour UI
        updateGridDisplay();
    }

    /**
     * Met à jour la grille (Tri + Rendu) sans rappeler la DB
     */
    private void updateGridDisplay() {
        eventsGrid.removeAll();

        if (currentResults == null || currentResults.isEmpty()) {
            resultsCount.setText("0 résultat trouvé");
            showEmptyState();
            return;
        }

        resultsCount.setText(currentResults.size() + " événements trouvés");

        // 1. Festivent du Tri (In-Memory)
        String sortOption = sortSelect.getValue();
        Comparator<Event> comparator;

        switch (sortOption) {
            case "Prix (Croissant)":
                comparator = Comparator.comparing(Event::getPrixUnitaire);
                break;
            case "Prix (Décroissant)":
                comparator = Comparator.comparing(Event::getPrixUnitaire).reversed();
                break;
            case "Nom (A-Z)":
                comparator = Comparator.comparing(Event::getTitre, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Date (Proche -> Loin)":
            default:
                comparator = Comparator.comparing(Event::getDateDebut);
                break;
        }

        List<Event> sortedEvents = currentResults.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        // 2. Création des cartes
        for (Event event : sortedEvents) {
            EventCard card = new EventCard(event);
            card.setOnView(() -> navigationManager.goToEventDetail(event.getId()));
            eventsGrid.add(card);
        }
    }

    private void showEmptyState() {
        Div emptyDiv = new Div();
        emptyDiv.setText("Aucun événement ne correspond à vos critères.");
        emptyDiv.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.TextAlignment.CENTER, LumoUtility.Padding.LARGE);
        // Centrer le message dans la grille ou le conteneur
        emptyDiv.getStyle().set("grid-column", "1 / -1");
        eventsGrid.add(emptyDiv);
    }
}