package ma.projet.events. ui.view.client;

import com. vaadin.flow. component.UI;
import com.vaadin.flow.component. button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com. vaadin.flow. component.orderedlayout.FlexComponent;
import com. vaadin.flow. component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router. PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security. PermitAll;
import ma.projet.events. entity. Reservation;
import ma.projet.events. entity.ReservationStatus;
import ma.projet. events.entity.User;
import ma. projet.events.security.SecurityService;
import ma.projet.events. service.ReservationService;
import ma. projet.events.service.UserService;
import ma.projet. events.ui.layout.MainLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util. List;
import java.util. Locale;
import java.util. Map;

/**
 * Dashboard client - Vue principale après connexion
 */
@Route(value = "client/dashboard", layout = MainLayout.class)
@PageTitle("Dashboard - EventReserve")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private final ReservationService reservationService;

    private final User currentUser;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    public DashboardView(SecurityService securityService,
                         UserService userService,
                         ReservationService reservationService) {
        this. securityService = securityService;
        this.userService = userService;
        this.reservationService = reservationService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");

        // Contenu principal
        add(
                createWelcomeSection(),
                createStatsSection(),
                createQuickActionsSection(),
                createRecentReservationsSection()
        );
    }

    /**
     * Section de bienvenue
     */
    private VerticalLayout createWelcomeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("margin-bottom", "var(--festivent-space-xl)");

        H2 welcomeTitle = new H2("Welcome back, " + currentUser.getPrenom() + "!");
        welcomeTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Here's an overview of your event reservations");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        section.add(welcomeTitle, subtitle);
        return section;
    }

    /**
     * Section des statistiques (3 cartes)
     */
    private HorizontalLayout createStatsSection() {
        // Récupérer les statistiques
        Map<String, Object> stats = userService. getUserStatistics(currentUser. getId());
        List<Reservation> reservations = reservationService.findUserReservations(currentUser.getId());

        // Calculer les événements à venir
        long upcomingEvents = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .filter(r -> r. getEvenement().getDateDebut().isAfter(LocalDateTime.now()))
                .count();

        // Créer les cartes
        HorizontalLayout statsSection = new HorizontalLayout();
        statsSection.setWidthFull();
        statsSection.setSpacing(true);
        statsSection.getStyle()
                .set("gap", "var(--festivent-space-lg)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        statsSection.add(
                createStatCard("Total Reservations",
                        String.valueOf(stats.get("totalReservations")),
                        VaadinIcon. TICKET),
                createStatCard("Upcoming Events",
                        String.valueOf(upcomingEvents),
                        VaadinIcon.CALENDAR),
                createStatCard("Total Spent",
                        String.format("€%.2f", (Double) stats.get("montantTotalDepense")),
                        VaadinIcon.EURO)
        );

        return statsSection;
    }

    /**
     * Crée une carte de statistique
     */
    private Div createStatCard(String label, String value, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("flex", "1")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("padding", "var(--festivent-space-lg)");

        // Contenu gauche (label + valeur)
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

        // Card "My Reservations"
        Div reservationsCard = createQuickActionCard(
                "My Reservations",
                "View and manage your event reservations",
                "View All",
                VaadinIcon.ARROW_RIGHT,
                true,
                "client/reservations"
        );

        // Card "Browse Events"
        Div eventsCard = createQuickActionCard(
                "Browse Events",
                "Discover new events to attend",
                "Explore",
                VaadinIcon. ARROW_RIGHT,
                false,
                "events"
        );

        section.add(reservationsCard, eventsCard);
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
            button. addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        } else {
            button.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
            button.getStyle().set("color", "var(--festivent-secondary-text)");
        }
        button. addClickListener(e -> UI.getCurrent().navigate(route));

        card.add(content, button);
        return card;
    }

    /**
     * Section des réservations récentes
     */
    private Div createRecentReservationsSection() {
        Div section = new Div();
        section.addClassName("festivent-card");
        section.getStyle().set("padding", "var(--festivent-space-lg)");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.setAlignItems(FlexComponent. Alignment.CENTER);
        header.getStyle().set("margin-bottom", "var(--festivent-space-md)");

        H3 title = new H3("Recent Reservations");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Anchor viewAll = new Anchor("client/reservations", "View all");
        viewAll. getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500")
                .set("text-decoration", "none");

        header.add(title, viewAll);

        // Liste des réservations
        VerticalLayout reservationsList = new VerticalLayout();
        reservationsList.setPadding(false);
        reservationsList.setSpacing(false);

        List<Reservation> reservations = reservationService.findUserReservations(currentUser.getId());

        // Afficher les 3 dernières réservations
        reservations.stream()
                .sorted((r1, r2) -> r2.getDateReservation().compareTo(r1.getDateReservation()))
                .limit(3)
                .forEach(reservation -> reservationsList.add(createReservationRow(reservation)));

        if (reservations.isEmpty()) {
            Span emptyMessage = new Span("No reservations yet.  Start exploring events!");
            emptyMessage.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic")
                    .set("padding", "var(--festivent-space-lg)")
                    .set("text-align", "center")
                    .set("display", "block");
            reservationsList. add(emptyMessage);
        }

        section.add(header, reservationsList);
        return section;
    }

    /**
     * Crée une ligne de réservation
     */
    private HorizontalLayout createReservationRow(Reservation reservation) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment. CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        row.getStyle()
                .set("padding", "var(--festivent-space-md) 0")
                .set("border-bottom", "1px solid var(--festivent-secondary)");

        // Info gauche
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span eventTitle = new Span(reservation.getEvenement().getTitre());
        eventTitle.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-s)");

        String dateInfo = reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)
                + " • " + reservation.getNombrePlaces() + " seats";
        Span dateSpan = new Span(dateInfo);
        dateSpan. getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        info.add(eventTitle, dateSpan);

        // Info droite (prix + statut)
        VerticalLayout priceStatus = new VerticalLayout();
        priceStatus.setPadding(false);
        priceStatus. setSpacing(false);
        priceStatus. setAlignItems(FlexComponent. Alignment.END);

        Span price = new Span(String.format("€%.2f", reservation.getMontantTotal()));
        price.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-s)");

        Span status = new Span(reservation.getStatut().getLabel());
        status.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", getStatusColor(reservation. getStatut()));

        priceStatus.add(price, status);

        row.add(info, priceStatus);
        return row;
    }

    /**
     * Retourne la couleur selon le statut
     */
    private String getStatusColor(ReservationStatus status) {
        return switch (status) {
            case CONFIRMEE -> "#22c55e"; // Vert
            case EN_ATTENTE -> "#f59e0b"; // Orange
            case ANNULEE -> "#ef4444"; // Rouge
        };
    }
}