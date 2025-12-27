package ma.projet.events.ui.view. client;

import com.vaadin.flow.component.UI;
import com. vaadin.flow. component.button.Button;
import com. vaadin.flow. component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin. flow.component.grid.Grid;
import com.vaadin. flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin. flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow. component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin. flow.data.value.ValueChangeMode;
import com. vaadin.flow. router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import ma.projet.events. entity. Reservation;
import ma.projet.events. entity.ReservationStatus;
import ma. projet.events.entity.User;
import ma.projet.events.exception.BusinessException;
import ma.projet.events. security.SecurityService;
import ma. projet.events.service.ReservationService;
import ma. projet.events.ui.layout.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util. List;
import java.util. Locale;
import java.util. stream.Collectors;

/**
 * Vue "Mes Réservations" pour les clients
 */
@Route(value = "client/reservations", layout = MainLayout.class)
@PageTitle("My Reservations - EventReserve")
@PermitAll
public class MyReservationsView extends VerticalLayout {

    private final SecurityService securityService;
    private final ReservationService reservationService;
    private final User currentUser;

    // Composants UI
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;
    private Grid<Reservation> grid;
    private Span countLabel;

    // Données
    private List<Reservation> allReservations;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    public MyReservationsView(SecurityService securityService,
                              ReservationService reservationService) {
        this. securityService = securityService;
        this.reservationService = reservationService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");

        add(
                createHeaderSection(),
                createFiltersSection(),
                createGridSection()
        );

        // Charger les données
        loadReservations();
    }

    /**
     * Section header avec titre
     */
    private VerticalLayout createHeaderSection() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("margin-bottom", "var(--festivent-space-lg)");

        H2 title = new H2("My Reservations");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("View and manage your event reservations");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        header.add(title, subtitle);
        return header;
    }

    /**
     * Section des filtres (recherche + statut)
     */
    private HorizontalLayout createFiltersSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment. END);
        filters.setSpacing(true);
        filters.getStyle()
                .set("gap", "var(--festivent-space-md)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        // Champ de recherche
        searchField = new TextField();
        searchField. setPlaceholder("Search by code or event.. .");
        searchField.setPrefixComponent(VaadinIcon. SEARCH. create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());
        searchField.getStyle()
                .set("flex", "1")
                .set("max-width", "none");

        // Filtre par statut
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
        section.setSizeFull();
        section.getStyle().set("padding", "var(--festivent-space-lg)");

        // Header de la grille avec compteur
        HorizontalLayout gridHeader = new HorizontalLayout();
        gridHeader.setWidthFull();
        gridHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        gridHeader.getStyle().set("margin-bottom", "var(--festivent-space-md)");

        countLabel = new Span("Reservations (0)");
        countLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        gridHeader.add(countLabel);

        // Création de la grille
        grid = new Grid<>(Reservation.class, false);
        grid.setWidthFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant. LUMO_NO_BORDER, GridVariant. LUMO_ROW_STRIPES);

        // Colonnes
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addComponentColumn(reservation -> {
            Anchor eventLink = new Anchor(
                    "event/" + reservation.getEvenement().getId(),
                    reservation.getEvenement().getTitre()
            );
            eventLink.getStyle()
                    .set("color", "var(--festivent-primary)")
                    .set("font-weight", "500")
                    .set("text-decoration", "none");
            return eventLink;
        }).setHeader("Event").setFlexGrow(2);

        grid.addColumn(reservation ->
                        reservation.getEvenement().getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Seats")
                .setFlexGrow(0)
                .setWidth("80px")
                .setSortable(true);

        grid.addColumn(reservation ->
                        String.format("€%.2f", reservation.getMontantTotal()))
                .setHeader("Amount")
                .setFlexGrow(0)
                .setWidth("100px")
                .setSortable(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addComponentColumn(this::createActionsColumn)
                .setHeader("Actions")
                .setFlexGrow(0)
                .setWidth("100px");

        section.add(gridHeader, grid);
        return section;
    }

    /**
     * Crée le badge de statut
     */
    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().getLabel());

        String backgroundColor;
        String textColor;
        String borderColor;

        switch (reservation. getStatut()) {
            case CONFIRMEE -> {
                backgroundColor = "#dcfce7";
                textColor = "#166534";
                borderColor = "#22c55e";
            }
            case EN_ATTENTE -> {
                backgroundColor = "transparent";
                textColor = "#6b7280";
                borderColor = "#d1d5db";
            }
            case ANNULEE -> {
                backgroundColor = "#fee2e2";
                textColor = "#991b1b";
                borderColor = "#ef4444";
            }
            default -> {
                backgroundColor = "#f3f4f6";
                textColor = "#6b7280";
                borderColor = "#d1d5db";
            }
        }

        badge. getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "9999px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "500")
                .set("background-color", backgroundColor)
                .set("color", textColor)
                .set("border", "1px solid " + borderColor);

        return badge;
    }

    /**
     * Crée la colonne des actions
     */
    private HorizontalLayout createActionsColumn(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("gap", "var(--festivent-space-xs)");

        // Bouton voir détails
        Button viewButton = new Button(VaadinIcon.EYE. create());
        viewButton. addThemeVariants(ButtonVariant. LUMO_TERTIARY, ButtonVariant. LUMO_ICON);
        viewButton. getStyle().set("color", "var(--lumo-secondary-text-color)");
        viewButton.addClickListener(e -> showReservationDetails(reservation));

        // Bouton annuler (seulement si annulable)
        Button cancelButton = new Button(VaadinIcon. CLOSE. create());
        cancelButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        cancelButton.getStyle().set("color", "var(--lumo-secondary-text-color)");

        if (reservation.isAnnulable()) {
            cancelButton. addClickListener(e -> showCancelConfirmation(reservation));
        } else {
            cancelButton.setEnabled(false);
            cancelButton.getStyle().set("opacity", "0.3");
        }

        actions.add(viewButton, cancelButton);
        return actions;
    }

    /**
     * Charge les réservations
     */
    private void loadReservations() {
        allReservations = reservationService.findUserReservations(currentUser.getId());
        applyFilters();
    }

    /**
     * Applique les filtres
     */
    private void applyFilters() {
        String searchTerm = searchField.getValue().toLowerCase().trim();
        ReservationStatus status = statusFilter.getValue();

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> {
                    if (searchTerm. isEmpty()) return true;
                    return r.getCodeReservation().toLowerCase().contains(searchTerm) ||
                            r.getEvenement().getTitre().toLowerCase().contains(searchTerm);
                })
                .filter(r -> status == null || r.getStatut() == status)
                .collect(Collectors.toList());

        grid.setItems(filtered);
        countLabel.setText("Reservations (" + filtered.size() + ")");
    }

    /**
     * Affiche les détails d'une réservation
     */
    private void showReservationDetails(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Reservation Details");
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        content.add(createDetailRow("Code", reservation. getCodeReservation()));
        content.add(createDetailRow("Event", reservation. getEvenement().getTitre()));
        content.add(createDetailRow("Date", reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)));
        content.add(createDetailRow("Location", reservation.getEvenement().getLieu() + ", " + reservation.getEvenement().getVille()));
        content.add(createDetailRow("Seats", String.valueOf(reservation.getNombrePlaces())));
        content.add(createDetailRow("Amount", String.format("€%. 2f", reservation. getMontantTotal())));
        content.add(createDetailRow("Status", reservation. getStatut().getLabel()));
        content.add(createDetailRow("Booked on", reservation.getDateReservation().format(DATE_FORMATTER)));

        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isEmpty()) {
            content. add(createDetailRow("Comment", reservation. getCommentaire()));
        }

        dialog.add(content);

        Button closeButton = new Button("Close", e -> dialog.close());
        closeButton. addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    /**
     * Crée une ligne de détail
     */
    private HorizontalLayout createDetailRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);

        Span labelSpan = new Span(label);
        labelSpan. getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "600");

        row.add(labelSpan, valueSpan);
        return row;
    }

    /**
     * Affiche la confirmation d'annulation
     */
    private void showCancelConfirmation(Reservation reservation) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancel Reservation");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span message = new Span("Are you sure you want to cancel this reservation?");
        message.getStyle().set("color", "var(--festivent-secondary-text)");

        Span details = new Span(reservation.getEvenement().getTitre() + " - " +
                reservation. getNombrePlaces() + " seat(s)");
        details.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span warning = new Span("This action cannot be undone.");
        warning.getStyle()
                .set("color", "#ef4444")
                .set("font-size", "var(--lumo-font-size-s)");

        content.add(message, details, warning);
        dialog.add(content);

        Button cancelButton = new Button("Keep Reservation", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmButton = new Button("Cancel Reservation", e -> {
            try {
                reservationService.annulerReservation(reservation.getId(), currentUser.getId());
                Notification. show("Reservation cancelled successfully", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
                dialog.close();
                loadReservations(); // Recharger les données
            } catch (BusinessException ex) {
                Notification.show(ex.getMessage(), 4000, Notification. Position.TOP_CENTER)
                        . addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant. LUMO_ERROR);

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }
}