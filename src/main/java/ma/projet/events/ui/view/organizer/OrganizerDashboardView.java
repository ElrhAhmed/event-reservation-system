package ma.projet.events. ui.view.organizer;

import com.vaadin.flow.component.UI;
import com. vaadin.flow. component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security. RolesAllowed;
import ma.projet.events. entity.Event;
import ma.projet. events.entity.EventStatus;
import ma. projet.events.entity.User;
import ma. projet.events.security.SecurityService;
import ma. projet.events.service.EventService;
import ma.projet. events.service.ReservationService;
import ma.projet.events.ui.layout.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java. util. Locale;
import java.util. Map;

/**
 * Dashboard Organisateur - Vue principale
 */
@Route(value = "organizer/dashboard", layout = MainLayout.class)
@PageTitle("Organizer Dashboard - EventReserve")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;

    private final User currentUser;
    private List<Event> myEvents;
    private Map<String, Object> stats;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale. ENGLISH);
    private static final String DEFAULT_IMAGE =
            "https://images.unsplash. com/photo-1492684223066-81342ee5ff30?w=400";

    public OrganizerDashboardView(SecurityService securityService,
                                  EventService eventService,
                                  ReservationService reservationService) {
        this.eventService = eventService;
        this.currentUser = securityService.getAuthenticatedUser();

        // Charger les données
        loadData();

        // Configuration du layout
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");

        add(
                createHeaderSection(),
                createStatsSection(),
                createQuickActionsSection(),
                createRecentEventsSection()
        );
    }

    /**
     * Charge les données de l'organisateur
     */
    private void loadData() {
        myEvents = eventService. getEventsByOrganisateur(currentUser.getId());
        stats = eventService.getOrganizerStatistics(currentUser.getId());
    }

    /**
     * Section header avec titre et bouton Create Event
     */
    private HorizontalLayout createHeaderSection() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.setAlignItems(FlexComponent.Alignment. CENTER);
        header.getStyle().set("margin-bottom", "var(--festivent-space-xl)");

        // Titre
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection. setSpacing(false);

        H2 title = new H2("Organizer Dashboard");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Manage your events and reservations");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        titleSection.add(title, subtitle);

        // Bouton Create Event
        Button createEventButton = new Button("Create Event", VaadinIcon. PLUS. create());
        createEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createEventButton. addClickListener(e -> UI.getCurrent().navigate("organizer/event/new"));

        header.add(titleSection, createEventButton);
        return header;
    }

    /**
     * Section des statistiques (3 cartes)
     */
    private HorizontalLayout createStatsSection() {
        HorizontalLayout statsSection = new HorizontalLayout();
        statsSection.setWidthFull();
        statsSection.setSpacing(true);
        statsSection.getStyle()
                .set("gap", "var(--festivent-space-lg)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        // ✅ CORRECTION:  Utiliser Number pour gérer Integer et Long
        Number publishedCount = (Number) stats.getOrDefault("publishedEvents", 0);
        Number totalEvents = (Number) stats.getOrDefault("totalEvents", 0);
        Number totalReservations = (Number) stats.getOrDefault("totalReservations", 0);
        Number totalRevenue = (Number) stats.getOrDefault("totalRevenue", 0.0);

        statsSection.add(
                createStatCard(
                        "Events Created",
                        String.valueOf(totalEvents.longValue()),
                        publishedCount.longValue() + " published",
                        VaadinIcon. CALENDAR
                ),
                createStatCard(
                        "Total Reservations",
                        String.valueOf(totalReservations.longValue()),
                        null,
                        VaadinIcon.TICKET
                ),
                createStatCard(
                        "Total Revenue",
                        String. format("€%.2f", totalRevenue.doubleValue()),
                        null,
                        VaadinIcon.EURO
                )
        );

        return statsSection;
    }

    /**
     * Crée une carte de statistique
     */
    private Div createStatCard(String label, String value, String subValue, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "var(--festivent-space-lg)");

        // Contenu gauche
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("line-height", "1.2");

        content.add(labelSpan, valueSpan);

        if (subValue != null) {
            Span subValueSpan = new Span(subValue);
            subValueSpan. getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-xs)");
            content. add(subValueSpan);
        }

        // Icône à droite
        Div iconContainer = new Div();
        iconContainer. getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "48px")
                .set("height", "48px")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("background-color", "var(--festivent-accent)");

        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--festivent-primary)");
        iconContainer.add(icon);

        card.add(content, iconContainer);
        return card;
    }

    /**
     * Section des actions rapides
     */
    private HorizontalLayout createQuickActionsSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.setWidthFull();
        section.setSpacing(true);
        section.getStyle()
                .set("gap", "var(--festivent-space-lg)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        // Card "My Events"
        Div eventsCard = createQuickActionCard(
                "My Events",
                "View and manage all your events",
                "View All",
                VaadinIcon.ARROW_RIGHT,
                true,
                "organizer/events"
        );

        // Card "Create New Event"
        Div createCard = createQuickActionCard(
                "Create New Event",
                "Add a new event to your catalog",
                "Create",
                VaadinIcon.PLUS,
                false,
                "organizer/event/new"
        );

        section.add(eventsCard, createCard);
        return section;
    }

    /**
     * Crée une carte d'action rapide
     */
    private Div createQuickActionCard(String title, String description,
                                      String buttonText, VaadinIcon buttonIcon,
                                      boolean isPrimary, String route) {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "var(--festivent-space-lg)");

        // Contenu gauche
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600");

        Span descSpan = new Span(description);
        descSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        content.add(titleSpan, descSpan);

        // Bouton
        Button button = new Button(buttonText, buttonIcon. create());
        button.setIconAfterText(true);
        if (isPrimary) {
            button. addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            button.getStyle()
                    .set("border", "1px solid var(--festivent-secondary)")
                    .set("color", "var(--festivent-secondary-text)");
        }
        button. addClickListener(e -> UI.getCurrent().navigate(route));

        card.add(content, button);
        return card;
    }

    /**
     * Section des événements récents
     */
    private Div createRecentEventsSection() {
        Div section = new Div();
        section.addClassName("festivent-card");
        section.getStyle().set("padding", "var(--festivent-space-lg)");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent. JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment. CENTER);
        header.getStyle().set("margin-bottom", "var(--festivent-space-md)");

        H3 title = new H3("Recent Events");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Anchor viewAll = new Anchor("organizer/events", "View all");
        viewAll. getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500")
                .set("text-decoration", "none");

        header.add(title, viewAll);

        // Liste des événements
        VerticalLayout eventsList = new VerticalLayout();
        eventsList.setPadding(false);
        eventsList.setSpacing(false);

        // Afficher les 5 derniers événements
        myEvents.stream()
                .sorted((e1, e2) -> e2.getDateCreation().compareTo(e1.getDateCreation()))
                .limit(5)
                .forEach(event -> eventsList.add(createEventRow(event)));

        if (myEvents.isEmpty()) {
            Span emptyMessage = new Span("No events yet.  Create your first event!");
            emptyMessage.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic")
                    .set("padding", "var(--festivent-space-lg)")
                    .set("text-align", "center")
                    .set("display", "block");
            eventsList.add(emptyMessage);
        }

        section.add(header, eventsList);
        return section;
    }

    /**
     * Crée une ligne d'événement
     */
    private HorizontalLayout createEventRow(Event event) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment. CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        row.getStyle()
                .set("padding", "var(--festivent-space-md) 0")
                .set("border-bottom", "1px solid var(--festivent-secondary)")
                .set("cursor", "pointer");

        // Hover effect
        row.getElement().addEventListener("mouseenter", e ->
                row.getStyle().set("background-color", "var(--festivent-accent)"));
        row.getElement().addEventListener("mouseleave", e ->
                row.getStyle().set("background-color", "transparent"));

        // Click navigation
        row.addClickListener(e -> UI.getCurrent().navigate("organizer/event-reservations/" + event. getId()));

        // Image + Info
        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment. CENTER);
        leftSection.setSpacing(true);
        leftSection.getStyle().set("gap", "var(--festivent-space-md)");

        // Image
        String imageUrl = (event.getImageUrl() != null && !event. getImageUrl().isBlank())
                ? event. getImageUrl() : DEFAULT_IMAGE;
        Image image = new Image(imageUrl, event.getTitre());
        image.setWidth("48px");
        image.setHeight("48px");
        image.getStyle()
                .set("border-radius", "var(--festivent-radius-sm)")
                .set("object-fit", "cover");

        // Info
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span eventTitle = new Span(event.getTitre());
        eventTitle.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-s)");

        String dateInfo = event.getDateDebut().format(DATE_FORMATTER) + " • " + event.getVille();
        Span dateSpan = new Span(dateInfo);
        dateSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        info.add(eventTitle, dateSpan);
        leftSection.add(image, info);

        // Right section:  Seats + Status
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent. Alignment.CENTER);
        rightSection. setSpacing(true);
        rightSection.getStyle().set("gap", "var(--festivent-space-md)");

        // Seats info
        int availableSeats = eventService.calculateAvailablePlaces(event.getId());
        int totalSeats = event.getCapaciteMax();
        int bookedSeats = totalSeats - availableSeats;

        VerticalLayout seatsInfo = new VerticalLayout();
        seatsInfo. setPadding(false);
        seatsInfo.setSpacing(false);
        seatsInfo. setAlignItems(FlexComponent. Alignment.END);

        Span seatsValue = new Span(bookedSeats + "/" + totalSeats);
        seatsValue.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-s)");

        Span seatsLabel = new Span("seats");
        seatsLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        seatsInfo.add(seatsValue, seatsLabel);

        // Status badge
        Span statusBadge = createStatusBadge(event.getStatut());

        rightSection.add(seatsInfo, statusBadge);

        row.add(leftSection, rightSection);
        return row;
    }

    /**
     * Crée un badge de statut
     */
    private Span createStatusBadge(EventStatus status) {
        Span badge = new Span(status.getLabel());

        String backgroundColor;
        String textColor;

        switch (status) {
            case PUBLIE -> {
                backgroundColor = "#dcfce7";
                textColor = "#166534";
            }
            case BROUILLON -> {
                backgroundColor = "#fef3c7";
                textColor = "#92400e";
            }
            case ANNULE -> {
                backgroundColor = "#fee2e2";
                textColor = "#991b1b";
            }
            case TERMINE -> {
                backgroundColor = "#f3f4f6";
                textColor = "#6b7280";
            }
            default -> {
                backgroundColor = "#f3f4f6";
                textColor = "#6b7280";
            }
        }

        badge.getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "9999px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "500")
                .set("background-color", backgroundColor)
                .set("color", textColor);

        return badge;
    }
}