package ma.projet.events.ui.view.client;

import com.vaadin.flow. component.UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button. ButtonVariant;
import com. vaadin.flow.component.html.H2;
import com.vaadin. flow.component.html.H3;
import com.vaadin.flow.component.html. Paragraph;
import com.vaadin. flow.component.html.Span;
import com. vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow. router.PageTitle;
import com.vaadin.flow.router. Route;
import ma.projet.events.entity.Event;
import ma.projet.events.entity. Reservation;
import ma.projet.events.entity.ReservationStatus;
import ma. projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events. ui.component.EventCard;
import ma.projet.events.ui. layout.ClientLayout;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util. Locale;
import java.util.stream. Collectors;

/**
 * Tableau de bord client
 * Route : /client/dashboard
 *
 * Affiche :
 * - Statistiques des réservations
 * - Prochaines réservations
 * - Événements suggérés
 *
 * Phase 5 : Utilisateur simulé (ID = 4 par défaut = Client 1 dans DataInit)
 * Phase 10 : Utilisateur réel via Spring Security
 */
@Route(value = "client/dashboard", layout = ClientLayout.class)
@PageTitle("Tableau de bord - Festivent")
public class DashboardView extends VerticalLayout {

    // TODO Phase 10 : Récupérer l'ID du vrai utilisateur connecté
    private static final Long SIMULATED_USER_ID = 4L; // Client 1 dans DataInit. java

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final ReservationService reservationService;
    private final EventService eventService;

    public DashboardView(ReservationService reservationService, EventService eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background-color", "var(--festivent-bg)");

        // Titre
        H2 title = new H2("Tableau de bord");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Message de bienvenue
        Paragraph welcome = new Paragraph("Bienvenue sur votre espace personnel !  Gérez vos réservations et découvrez de nouveaux événements.");
        welcome.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("margin", "0 0 var(--lumo-space-xl) 0");

        // Statistiques (3 cards)
        HorizontalLayout stats = createStatisticsSection();

        // Prochaines réservations
        VerticalLayout upcomingReservations = createUpcomingReservationsSection();

        // Événements suggérés
        VerticalLayout suggestedEvents = createSuggestedEventsSection();

        add(title, welcome, stats, upcomingReservations, suggestedEvents);
    }

    /**
     * Crée la section statistiques (3 cards)
     */
    private HorizontalLayout createStatisticsSection() {
        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setSpacing(true);
        container.getStyle().set("flex-wrap", "wrap");

        try {
            // Récupérer toutes les réservations de l'utilisateur
            List<Reservation> allReservations = reservationService. findUserReservations(SIMULATED_USER_ID);

            // Calculer les statistiques
            long totalReservations = allReservations.size();

            long activeReservations = allReservations.stream()
                    .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                    .count();

            long cancelledReservations = allReservations.stream()
                    .filter(r -> r.getStatut() == ReservationStatus. ANNULEE)
                    .count();

            // Card 1 : Total réservations
            VerticalLayout totalCard = createStatCard(
                    VaadinIcon. TICKET.create(),
                    String.valueOf(totalReservations),
                    "Réservations totales",
                    "var(--festivent-primary)"
            );

            // Card 2 : Réservations actives
            VerticalLayout activeCard = createStatCard(
                    VaadinIcon.CHECK_CIRCLE.create(),
                    String.valueOf(activeReservations),
                    "Réservations actives",
                    "var(--festivent-success)"
            );

            // Card 3 : Réservations annulées
            VerticalLayout cancelledCard = createStatCard(
                    VaadinIcon. CLOSE_CIRCLE.create(),
                    String.valueOf(cancelledReservations),
                    "Réservations annulées",
                    "var(--festivent-error)"
            );

            container.add(totalCard, activeCard, cancelledCard);

        } catch (Exception e) {
            Paragraph error = new Paragraph("❌ Erreur lors du chargement des statistiques : " + e.getMessage());
            error. getStyle().set("color", "var(--festivent-error)");
            container.add(error);
        }

        return container;
    }

    /**
     * Crée une card de statistique
     */
    private VerticalLayout createStatCard(Icon icon, String value, String label, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.addClassName("festivent-card");
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "200px");

        // Icône
        icon.setSize("32px");
        icon.getStyle().set("color", color);

        // Valeur
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "700")
                .set("color", color)
                .set("margin", "var(--lumo-space-s) 0");

        // Label
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        card.add(icon, valueSpan, labelSpan);
        return card;
    }

    /**
     * Crée la section "Mes prochaines réservations"
     */
    private VerticalLayout createUpcomingReservationsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setSpacing(true);

        // Header de la section
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 sectionTitle = new H3("Mes prochaines réservations");
        sectionTitle.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        Button viewAllButton = new Button("Voir tout", VaadinIcon.ARROW_RIGHT.create());
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllButton.addClickListener(e -> {
            // TODO : Navigation vers MyReservationsView
            UI. getCurrent().navigate("client/reservations");
        });

        header.add(sectionTitle, viewAllButton);

        // Container des réservations
        VerticalLayout reservationsContainer = new VerticalLayout();
        reservationsContainer.setWidthFull();
        reservationsContainer.setSpacing(true);

        try {
            // Récupérer les réservations confirmées et futures
            List<Reservation> upcomingReservations = reservationService.findUserReservations(SIMULATED_USER_ID)
                    .stream()
                    .filter(r -> r. getStatut() == ReservationStatus.CONFIRMEE)
                    .filter(r -> r.getEvenement().getDateDebut().isAfter(java.time.LocalDateTime.now()))
                    .sorted((r1, r2) -> r1.getEvenement().getDateDebut().compareTo(r2.getEvenement().getDateDebut()))
                    . limit(3) // Afficher max 3
                    .collect(Collectors. toList());

            if (upcomingReservations.isEmpty()) {
                // Aucune réservation
                VerticalLayout emptyState = createEmptyState(
                        VaadinIcon. CALENDAR. create(),
                        "Aucune réservation à venir",
                        "Découvrez nos événements et réservez vos places !"
                );
                reservationsContainer.add(emptyState);
            } else {
                // Afficher les réservations
                for (Reservation reservation : upcomingReservations) {
                    HorizontalLayout reservationCard = createReservationCard(reservation);
                    reservationsContainer. add(reservationCard);
                }
            }

        } catch (Exception e) {
            Paragraph error = new Paragraph("❌ Erreur lors du chargement des réservations : " + e.getMessage());
            error.getStyle().set("color", "var(--festivent-error)");
            reservationsContainer. add(error);
        }

        section.add(header, reservationsContainer);
        return section;
    }

    /**
     * Crée une card de réservation compacte
     */
    private HorizontalLayout createReservationCard(Reservation reservation) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassName("festivent-card");
        card.setAlignItems(Alignment.CENTER);

        // Icône catégorie
        Icon categoryIcon = VaadinIcon.CALENDAR. create();
        categoryIcon.setSize("32px");
        categoryIcon.getStyle().set("color", "var(--festivent-primary)");

        // Informations événement
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);
        info.getStyle().set("flex", "1");

        Span eventTitle = new Span(reservation.getEvenement().getTitre());
        eventTitle. getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--festivent-text-primary)");

        Span eventDate = new Span(
                "📅 " + reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)
        );
        eventDate.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)");

        Span places = new Span(reservation.getNombrePlaces() + " place(s) • Code : " + reservation.getCodeReservation());
        places.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-tertiary)");

        info.add(eventTitle, eventDate, places);

        // Bouton détails
        Button detailsButton = new Button("Détails", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailsButton.addClickListener(e -> {
            // TODO : Navigation vers détails réservation
            UI.getCurrent().navigate("client/reservations");
        });

        card.add(categoryIcon, info, detailsButton);
        return card;
    }

    /**
     * Crée la section "Événements suggérés"
     */
    private VerticalLayout createSuggestedEventsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setSpacing(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 sectionTitle = new H3("Événements suggérés");
        sectionTitle.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        Button viewAllButton = new Button("Voir tout", VaadinIcon. ARROW_RIGHT.create());
        viewAllButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        viewAllButton.addClickListener(e -> {
            UI.getCurrent().navigate("events");
        });

        header.add(sectionTitle, viewAllButton);

        // Container des événements
        HorizontalLayout eventsContainer = new HorizontalLayout();
        eventsContainer.setWidthFull();
        eventsContainer.setSpacing(true);
        eventsContainer.getStyle().set("flex-wrap", "wrap");

        try {
            // Récupérer 3 événements à venir
            List<Event> suggestedEvents = eventService.searchWithFilters(null, null, null, null, null, null, null)
                    .stream()
                    .limit(3)
                    .collect(Collectors.toList());

            if (suggestedEvents.isEmpty()) {
                VerticalLayout emptyState = createEmptyState(
                        VaadinIcon.INFO_CIRCLE.create(),
                        "Aucun événement disponible",
                        "Revenez plus tard pour découvrir nos prochains événements"
                );
                eventsContainer.add(emptyState);
            } else {
                for (Event event :  suggestedEvents) {
                    EventCard eventCard = new EventCard(event);
                    eventCard.addClickListener(clickEvent -> {
                        UI.getCurrent().navigate("event/" + event.getId());
                    });
                    eventsContainer.add(eventCard);
                }
            }

        } catch (Exception e) {
            Paragraph error = new Paragraph("❌ Erreur lors du chargement des événements : " + e.getMessage());
            error. getStyle().set("color", "var(--festivent-error)");
            eventsContainer.add(error);
        }

        section.add(header, eventsContainer);
        return section;
    }

    /**
     * Crée un état vide (empty state)
     */
    private VerticalLayout createEmptyState(Icon icon, String title, String message) {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setWidthFull();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setPadding(true);
        emptyState.setSpacing(true);

        icon.setSize("48px");
        icon.getStyle().set("color", "var(--festivent-text-tertiary)");

        Span titleSpan = new Span(title);
        titleSpan. getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-text-secondary)");

        Span messageSpan = new Span(message);
        messageSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-align", "center");

        emptyState.add(icon, titleSpan, messageSpan);
        return emptyState;
    }
}