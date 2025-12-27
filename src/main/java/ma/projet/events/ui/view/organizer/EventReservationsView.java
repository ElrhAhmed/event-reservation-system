package ma.projet. events.ui.view.organizer;

import com.vaadin.flow.component.UI;
import com. vaadin.flow. component.button.Button;
import com. vaadin.flow. component.button.ButtonVariant;
import com.vaadin. flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid. Grid;
import com.vaadin.flow.component.grid. GridVariant;
import com.vaadin.flow.component. html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component. notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.component. textfield.TextField;
import com. vaadin.flow. data.value.ValueChangeMode;
import com. vaadin.flow. router.*;
import jakarta.annotation.security. RolesAllowed;
import ma.projet.events. entity.Event;
import ma. projet.events.entity.Reservation;
import ma.projet. events.entity.ReservationStatus;
import ma. projet.events.entity.User;
import ma. projet.events.exception.ResourceNotFoundException;
import ma.projet. events.security.SecurityService;
import ma. projet.events.service.EventService;
import ma.projet. events.service.ReservationService;
import ma. projet.events.ui.layout.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util. ArrayList;
import java.util.List;
import java.util. Locale;
import java.util.stream. Collectors;

/**
 * Vue des réservations d'un événement pour l'organisateur
 * Route: /organizer/event-reservations/{eventId}
 */
@Route(value = "organizer/event-reservations", layout = MainLayout.class)
@PageTitle("Event Reservations - EventReserve")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class EventReservationsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final User currentUser;

    // Données
    private Long eventId;
    private Event event;
    private List<Reservation> allReservations = new ArrayList<>();

    // Composants UI
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;
    private Grid<Reservation> grid;
    private Span countLabel;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale. ENGLISH);

    public EventReservationsView(SecurityService securityService,
                                 EventService eventService,
                                 ReservationService reservationService) {
        this. securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Long eventId) {
        if (eventId == null) {
            Notification. show("Event ID is required", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            beforeEvent.rerouteTo("organizer/events");
            return;
        }

        try {
            this. eventId = eventId;
            this.event = eventService.getEventById(eventId);

            // Vérifier que l'utilisateur est bien l'organisateur ou admin
            if (!event.getOrganisateur().getId().equals(currentUser.getId()) &&
                    !currentUser.isAdmin()) {
                Notification.show("Access denied", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_ERROR);
                beforeEvent.rerouteTo("organizer/events");
                return;
            }

            loadData();
            buildUI();

        } catch (ResourceNotFoundException e) {
            Notification.show("Event not found", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            beforeEvent.rerouteTo("organizer/events");
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error:  " + e.getMessage(), 4000, Notification. Position.TOP_CENTER)
                    . addThemeVariants(NotificationVariant.LUMO_ERROR);
            beforeEvent.rerouteTo("organizer/events");
        }
    }

    /**
     * Charge les données
     */
    private void loadData() {
        allReservations = reservationService.findEventReservations(eventId);
        if (allReservations == null) {
            allReservations = new ArrayList<>();
        }
    }

    /**
     * Construit l'interface
     */
    private void buildUI() {
        removeAll();

        add(
                createHeaderSection(),
                createStatsSection(),
                createFiltersSection(),
                createGridSection()
        );
    }

    /**
     * Section header avec back link et titre
     */
    private VerticalLayout createHeaderSection() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("margin-bottom", "var(--festivent-space-lg)");

        // Back link
        HorizontalLayout backNav = new HorizontalLayout();
        backNav.setPadding(false);
        backNav. setSpacing(true);
        backNav.setAlignItems(FlexComponent.Alignment.CENTER);
        backNav.getStyle()
                .set("cursor", "pointer")
                .set("gap", "0. 5rem")
                .set("margin-bottom", "var(--festivent-space-sm)");

        Icon backIcon = VaadinIcon. ARROW_LEFT.create();
        backIcon.setSize("18px");
        backIcon. getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span backLabel = new Span("Back to My Events");
        backLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        backNav.add(backIcon, backLabel);
        backNav.addClickListener(e -> UI.getCurrent().navigate("organizer/events"));

        // Hover effect
        backNav.getElement().addEventListener("mouseenter", ev -> {
            backIcon.getStyle().set("color", "var(--festivent-primary)");
            backLabel.getStyle().set("color", "var(--festivent-primary)");
        });
        backNav.getElement().addEventListener("mouseleave", ev -> {
            backIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");
            backLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        });

        // Title
        H2 title = new H2(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Reservations for this event");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        header.add(backNav, title, subtitle);
        return header;
    }

    /**
     * Section des statistiques
     */
    private HorizontalLayout createStatsSection() {
        HorizontalLayout statsSection = new HorizontalLayout();
        statsSection.setWidthFull();
        statsSection.setSpacing(true);
        statsSection.getStyle()
                .set("gap", "var(--festivent-space-lg)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        // Calculer les stats depuis les réservations
        int totalReservations = allReservations. size();

        int totalPlaces = allReservations.stream()
                .filter(r -> r. getStatut() != ReservationStatus. ANNULEE)
                .mapToInt(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .sum();

        double totalRevenue = allReservations.stream()
                .filter(r -> r. getStatut() == ReservationStatus. CONFIRMEE)
                .mapToDouble(r -> r. getMontantTotal() != null ? r. getMontantTotal() : 0.0)
                .sum();

        int capaciteMax = event.getCapaciteMax() != null ? event. getCapaciteMax() : 0;

        statsSection.add(
                createStatCard("Total Reservations", String.valueOf(totalReservations), VaadinIcon. TICKET),
                createStatCard("Seats Booked", totalPlaces + "/" + capaciteMax, VaadinIcon. USERS),
                createStatCard("Revenue", String.format("€%.2f", totalRevenue), VaadinIcon.EURO)
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

        Div iconContainer = new Div();
        iconContainer.getStyle()
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
     * Section des filtres
     */
    private HorizontalLayout createFiltersSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment. END);
        filters.setSpacing(true);
        filters.getStyle()
                .set("gap", "var(--festivent-space-md)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        searchField = new TextField();
        searchField.setPlaceholder("Search by code or name...");
        searchField.setPrefixComponent(VaadinIcon. SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField. setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>();
        statusFilter. setPlaceholder("All Status");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());
        statusFilter.setWidth("180px");

        filters.add(searchField, statusFilter);
        filters.setFlexGrow(1, searchField);

        return filters;
    }

    /**
     * Section de la grille
     */
    private Div createGridSection() {
        Div section = new Div();
        section.addClassName("festivent-card");
        section.getStyle().set("padding", "var(--festivent-space-lg)");

        countLabel = new Span("Reservations (0)");
        countLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin-bottom", "var(--festivent-space-md)")
                .set("display", "block");

        grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();
        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant. LUMO_ROW_STRIPES);

        // Colonne Code
        grid.addColumn(reservation -> {
                    if (reservation == null) return "-";
                    String code = reservation.getCodeReservation();
                    return (code != null && ! code.isEmpty()) ? code : "-";
                })
                .setHeader("Code")
                .setFlexGrow(1)
                .setSortable(true);

        // Colonne Customer
        grid.addColumn(reservation -> {
                    if (reservation == null) return "Unknown";
                    User user = reservation.getUtilisateur();
                    if (user == null) return "Unknown";

                    String prenom = user.getPrenom();
                    String nom = user. getNom();

                    StringBuilder sb = new StringBuilder();
                    if (prenom != null && !prenom.trim().isEmpty()) {
                        sb.append(prenom. trim());
                    }
                    if (nom != null && !nom.trim().isEmpty()) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(nom.trim());
                    }

                    String fullName = sb.toString();
                    if (fullName. isEmpty()) {
                        String email = user.getEmail();
                        return (email != null && ! email.isEmpty()) ? email : "Unknown";
                    }
                    return fullName;
                })
                .setHeader("Customer")
                .setFlexGrow(2)
                .setSortable(true);

        // Colonne Date
        grid.addColumn(reservation -> {
                    if (reservation == null) return "-";
                    if (reservation.getDateReservation() == null) return "-";
                    try {
                        return reservation.getDateReservation().format(DATE_FORMATTER);
                    } catch (Exception e) {
                        return "-";
                    }
                })
                .setHeader("Date")
                .setFlexGrow(1)
                .setSortable(true);

        // Colonne Seats
        grid.addColumn(reservation -> {
                    if (reservation == null) return 0;
                    Integer places = reservation.getNombrePlaces();
                    return (places != null) ? places : 0;
                })
                .setHeader("Seats")
                .setFlexGrow(0)
                .setWidth("80px")
                .setSortable(true);

        // Colonne Amount
        grid.addColumn(reservation -> {
                    if (reservation == null) return "€0.00";
                    Double montant = reservation. getMontantTotal();
                    if (montant == null) return "€0.00";
                    try {
                        return String.format("€%. 2f", montant);
                    } catch (Exception e) {
                        return "€0.00";
                    }
                })
                .setHeader("Amount")
                .setFlexGrow(0)
                .setWidth("100px")
                .setSortable(true);

        // Colonne Status
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("120px");

        // Colonne Actions
        grid.addComponentColumn(this::createActionsColumn)
                .setHeader("Actions")
                .setFlexGrow(0)
                .setWidth("80px");

        applyFilters();

        section.add(countLabel, grid);
        return section;
    }

    /**
     * Crée le badge de statut
     */
    private Span createStatusBadge(Reservation reservation) {
        ReservationStatus status = null;
        if (reservation != null) {
            status = reservation.getStatut();
        }

        String label = (status != null) ? status.getLabel() : "Unknown";
        Span badge = new Span(label);

        String backgroundColor;
        String textColor;

        if (status == null) {
            backgroundColor = "#f3f4f6";
            textColor = "#6b7280";
        } else {
            switch (status) {
                case CONFIRMEE -> {
                    backgroundColor = "#dcfce7";
                    textColor = "#166534";
                }
                case EN_ATTENTE -> {
                    backgroundColor = "#fef3c7";
                    textColor = "#92400e";
                }
                case ANNULEE -> {
                    backgroundColor = "#fee2e2";
                    textColor = "#991b1b";
                }
                default -> {
                    backgroundColor = "#f3f4f6";
                    textColor = "#6b7280";
                }
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

    /**
     * Crée la colonne des actions
     */
    private HorizontalLayout createActionsColumn(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);
        actions.getStyle().set("gap", "var(--festivent-space-xs)");

        if (reservation == null) {
            return actions;
        }

        Button viewButton = new Button(VaadinIcon.EYE.create());
        viewButton. addThemeVariants(ButtonVariant. LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        viewButton.getStyle().set("color", "var(--lumo-secondary-text-color)");

        String email = "N/A";
        if (reservation.getUtilisateur() != null) {
            String userEmail = reservation.getUtilisateur().getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                email = userEmail;
            }
        }
        viewButton. getElement().setAttribute("title", "Email: " + email);

        actions.add(viewButton);
        return actions;
    }

    /**
     * Applique les filtres
     */
    private void applyFilters() {
        String searchTerm = "";
        if (searchField != null && searchField.getValue() != null) {
            searchTerm = searchField.getValue().toLowerCase().trim();
        }

        ReservationStatus status = (statusFilter != null) ? statusFilter.getValue() : null;

        final String finalSearchTerm = searchTerm;

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> r != null)
                .filter(r -> {
                    if (finalSearchTerm. isEmpty()) return true;

                    // Recherche par code
                    boolean matchesCode = false;
                    if (r.getCodeReservation() != null) {
                        matchesCode = r.getCodeReservation().toLowerCase().contains(finalSearchTerm);
                    }

                    // Recherche par nom
                    boolean matchesName = false;
                    if (r.getUtilisateur() != null) {
                        String prenom = r.getUtilisateur().getPrenom();
                        String nom = r.getUtilisateur().getNom();
                        if (prenom != null) {
                            matchesName = prenom.toLowerCase().contains(finalSearchTerm);
                        }
                        if (! matchesName && nom != null) {
                            matchesName = nom.toLowerCase().contains(finalSearchTerm);
                        }
                    }

                    // Recherche par email
                    boolean matchesEmail = false;
                    if (r.getUtilisateur() != null && r.getUtilisateur().getEmail() != null) {
                        matchesEmail = r. getUtilisateur().getEmail().toLowerCase().contains(finalSearchTerm);
                    }

                    return matchesCode || matchesName || matchesEmail;
                })
                .filter(r -> status == null || r.getStatut() == status)
                .collect(Collectors.toList());

        grid.setItems(filtered);
        countLabel.setText("Reservations (" + filtered.size() + ")");
    }
}