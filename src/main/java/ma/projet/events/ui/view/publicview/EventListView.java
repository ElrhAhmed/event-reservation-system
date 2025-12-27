package ma.projet.events.ui. view. publicview;
import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.combobox.ComboBox;
import com.vaadin.flow. component.html.Div;
import com. vaadin.flow.component.html.H1;
import com. vaadin.flow.component.html. Span;
import com.vaadin.flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import com.vaadin. flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.service.EventService;
import ma. projet.events.ui.component.EventCard;
import ma.projet.events.ui.component.EventFilterPanel;
import ma.projet.events.ui.layout.PublicLayout;

import java.time.LocalDateTime;
import java.util. Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Public event listing page - Modern marketplace UI.
 *
 * Layout:
 * - Centered content (max-width:  1400px)
 * - Two-column grid:  filters (left, narrow) + events (right, wide)
 * - Compact header with title and event count
 * - Dense, professional spacing
 * - 2-3 column responsive event grid
 */
@Route(value = "events", layout = PublicLayout.class)
@PageTitle("Browse Events - EventReserve")
@AnonymousAllowed
public class EventListView extends VerticalLayout {

    private final EventService eventService;

    // UI Components
    private final EventFilterPanel filterPanel;
    private final Div eventsGridContainer;
    private final Span eventCountSpan;
    private final Span resultCountSpan;
    private final ComboBox<SortOption> sortComboBox;

    // State
    private List<Event> allEvents;
    private List<Event> filteredEvents;
    private SortOption currentSortOption = SortOption.DATE_ASC;

    public EventListView(EventService eventService) {
        this.eventService = eventService;

        // Page-level configuration
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle()
                .set("background-color", "#f8f9fa")
                .set("padding-top", "2rem")
                .set("padding-bottom", "3rem");

        // Initialize components
        this.filterPanel = new EventFilterPanel();
        this.eventsGridContainer = new Div();
        this.eventCountSpan = new Span();
        this.resultCountSpan = new Span();
        this.sortComboBox = createSortComboBox();

        // Main centered container
        VerticalLayout mainContainer = createMainContainer();
        add(mainContainer);

        // Setup listeners
        setupFilterListeners();

        // Load initial data
        loadAllEvents();
    }

    /**
     * Creates the main centered container with max-width.
     */
    private VerticalLayout createMainContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container. setMaxWidth("1400px");
        container. setPadding(false);
        container.setSpacing(false);
        container.getStyle()
                .set("padding", "0 1.5rem");

        // Page header
        container.add(createPageHeader());

        // Two-column layout:  filters + events
        container.add(createTwoColumnLayout());

        return container;
    }

    /**
     * Creates the compact page header.
     */
    private VerticalLayout createPageHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle()
                .set("margin-bottom", "1.5rem");

        H1 title = new H1("Browse Events");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("line-height", "1.2");

        eventCountSpan.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "#718096")
                .set("margin-top", "0.25rem");

        header.add(title, eventCountSpan);
        return header;
    }

    /**
     * Creates the two-column grid layout.
     */
    private HorizontalLayout createTwoColumnLayout() {
        HorizontalLayout twoColumnLayout = new HorizontalLayout();
        twoColumnLayout.setWidthFull();
        twoColumnLayout.setSpacing(true);
        twoColumnLayout. setAlignItems(FlexComponent.Alignment.START);
        twoColumnLayout.getStyle()
                .set("gap", "1.5rem")
                .set("align-items", "flex-start");

        // Left column:  Filters (narrow)
        VerticalLayout filtersColumn = createFiltersColumn();

        // Right column: Events (wide)
        VerticalLayout eventsColumn = createEventsColumn();

        twoColumnLayout.add(filtersColumn, eventsColumn);
        twoColumnLayout.setFlexGrow(0, filtersColumn);  // Fixed width
        twoColumnLayout.setFlexGrow(1, eventsColumn);   // Flexible

        return twoColumnLayout;
    }

    /**
     * Creates the left filters column (narrow, fixed width).
     */
    private VerticalLayout createFiltersColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setWidth("280px");
        column.setPadding(false);
        column.setSpacing(false);

        // Card container for filters
        Div filterCard = new Div();
        filterCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "0.75rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("padding", "1.25rem");

        filterCard.add(filterPanel);
        column.add(filterCard);

        return column;
    }

    /**
     * Creates the right events column (wide, flexible).
     */
    private VerticalLayout createEventsColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setPadding(false);
        column.setSpacing(false);
        column.getStyle()
                .set("gap", "1.25rem")
                .set("flex", "1");

        // Toolbar
        column.add(createToolbar());

        // Events grid
        configureEventsGrid();
        column.add(eventsGridContainer);

        return column;
    }

    /**
     * Creates the compact toolbar with result count and sort selector.
     */
    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent. Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        toolbar.getStyle()
                .set("padding", "0.75rem 0")
                .set("border-bottom", "1px solid #e2e8f0");

        // Left:  Result count
        resultCountSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#718096");

        // Right: Sort selector
        HorizontalLayout sortSection = new HorizontalLayout();
        sortSection.setAlignItems(FlexComponent. Alignment.CENTER);
        sortSection.setSpacing(true);
        sortSection.getStyle().set("gap", "0.5rem");

        Span sortLabel = new Span("Sort by:");
        sortLabel.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568")
                .set("font-weight", "500");

        sortSection.add(sortLabel, sortComboBox);

        toolbar.add(resultCountSpan, sortSection);
        return toolbar;
    }

    /**
     * Configures the events grid with responsive columns.
     */
    private void configureEventsGrid() {
        eventsGridContainer.setWidthFull();
        eventsGridContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "1.25rem")
                .set("margin-top", "0.5rem");
    }

    /**
     * Creates the sort combo box.
     */
    private ComboBox<SortOption> createSortComboBox() {
        ComboBox<SortOption> comboBox = new ComboBox<>();
        comboBox.setItems(SortOption.values());
        comboBox.setItemLabelGenerator(SortOption:: getLabel);
        comboBox.setValue(SortOption.DATE_ASC);
        comboBox.setWidth("180px");
        comboBox.getStyle()
                .set("font-size", "0.875rem");

        comboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                currentSortOption = e.getValue();
                applySorting();
            }
        });

        return comboBox;
    }

    /**
     * Sets up filter panel listeners.
     */
    private void setupFilterListeners() {
        filterPanel.addApplyListener(e -> applyFilters());
        filterPanel.addResetListener(e -> {
            filterPanel.resetFilters();
            loadAllEvents();
        });
    }

    /**
     * Loads all published events.
     */
    private void loadAllEvents() {
        try {
            allEvents = eventService.getAllEvents().stream()
                    .filter(event -> event.getStatut() == EventStatus.PUBLIE)
                    .collect(Collectors.toList());

            filteredEvents = allEvents;
            applySorting();
            updateEventCount();
            displayEvents();

        } catch (Exception e) {
            handleError("Error loading events", e);
        }
    }

    /**
     * Applies filters from the filter panel.
     */
    private void applyFilters() {
        try {
            EventStatus status = filterPanel.getSelectedStatus();
            String city = filterPanel.getCity();
            LocalDateTime dateMin = filterPanel.getDateMin();
            LocalDateTime dateMax = filterPanel.getDateMax();
            Double priceMin = filterPanel.getPriceMin();
            Double priceMax = filterPanel.getPriceMax();

            boolean hasFilters = status != null || city != null ||
                    dateMin != null || dateMax != null ||
                    priceMin != null || priceMax != null;

            if (hasFilters) {
                filteredEvents = eventService.searchWithFilters(
                        null, city, dateMin, dateMax, null, priceMin, priceMax
                );

                if (status != null) {
                    filteredEvents = filteredEvents.stream()
                            .filter(event -> event.getStatut() == status)
                            .collect(Collectors.toList());
                }
            } else {
                filteredEvents = allEvents;
            }

            applySorting();
            updateEventCount();
            displayEvents();

            Notification.show(
                    filteredEvents.size() + " events found",
                    2000,
                    Notification.Position.TOP_CENTER
            );

        } catch (Exception e) {
            handleError("Error applying filters", e);
        }
    }

    /**
     * Applies current sorting option.
     */
    private void applySorting() {
        if (filteredEvents == null || filteredEvents.isEmpty()) {
            return;
        }

        switch (currentSortOption) {
            case DATE_ASC:
                filteredEvents.sort(Comparator.comparing(Event::getDateDebut));
                break;
            case DATE_DESC:
                filteredEvents.sort(Comparator.comparing(Event::getDateDebut).reversed());
                break;
            case PRICE_ASC:
                filteredEvents.sort(Comparator.comparing(Event::getPrixUnitaire));
                break;
            case PRICE_DESC:
                filteredEvents.sort(Comparator.comparing(Event::getPrixUnitaire).reversed());
                break;
            case NAME_ASC:
                filteredEvents.sort(Comparator. comparing(Event::getTitre));
                break;
            case NAME_DESC:
                filteredEvents.sort(Comparator.comparing(Event::getTitre).reversed());
                break;
        }

        displayEvents();
    }

    /**
     * Updates event count labels.
     */
    private void updateEventCount() {
        int total = allEvents != null ? allEvents.size() : 0;
        int filtered = filteredEvents != null ? filteredEvents.size() : 0;

        eventCountSpan.setText(filtered + " event" + (filtered != 1 ? "s" :  "") + " found");
        resultCountSpan.setText("Showing " + filtered + " of " + total + " events");
    }

    /**
     * Displays events in the grid.
     */
    private void displayEvents() {
        eventsGridContainer.removeAll();

        if (filteredEvents == null || filteredEvents.isEmpty()) {
            eventsGridContainer. add(createEmptyState());
        } else {
            for (Event event : filteredEvents) {
                EventCard card = new EventCard(event);
                card.addDetailsClickListener(e -> {
                    UI.getCurrent().navigate("event/" + event.getId());
                });
                eventsGridContainer.add(card);
            }
        }
    }

    /**
     * Creates empty state.
     */
    private Div createEmptyState() {
        Div emptyState = new Div();
        emptyState.getStyle()
                .set("grid-column", "1 / -1")
                .set("text-align", "center")
                .set("padding", "3rem 2rem")
                .set("background-color", "white")
                .set("border-radius", "0.75rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        Span icon = new Span("🔍");
        icon.getStyle()
                .set("font-size", "3rem")
                .set("display", "block")
                .set("margin-bottom", "1rem");

        Span title = new Span("No events found");
        title.getStyle()
                .set("display", "block")
                .set("font-size", "1.25rem")
                .set("font-weight", "600")
                .set("color", "#2d3748")
                .set("margin-bottom", "0.5rem");

        Span message = new Span("Try adjusting your filters or check back later.");
        message.getStyle()
                .set("display", "block")
                .set("color", "#718096")
                .set("font-size", "0.95rem");

        emptyState.add(icon, title, message);
        return emptyState;
    }

    /**
     * Handles errors gracefully.
     */
    private void handleError(String message, Exception e) {
        Notification notification = Notification.show(
                message + ": " + e.getMessage(),
                5000,
                Notification.Position.TOP_CENTER
        );
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        System.err.println(message + ": " + e. getMessage());
        e.printStackTrace();
    }

    /**
     * Sort options enum.
     */
    private enum SortOption {
        DATE_ASC("Date (Earliest First)"),
        DATE_DESC("Date (Latest First)"),
        PRICE_ASC("Price (Low to High)"),
        PRICE_DESC("Price (High to Low)"),
        NAME_ASC("Name (A-Z)"),
        NAME_DESC("Name (Z-A)");

        private final String label;

        SortOption(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}