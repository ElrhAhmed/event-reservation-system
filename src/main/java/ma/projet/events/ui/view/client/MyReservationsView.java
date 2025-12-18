package ma.projet.events.ui.view.client;

import com.vaadin.flow.component. UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. combobox.ComboBox;
import com.vaadin.flow. component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import ma.projet.events.entity. Reservation;
import ma.projet. events.entity.ReservationStatus;
import ma. projet.events.service.ReservationService;
import ma.projet.events.ui.component.StatusBadge;
import ma.projet.events.ui.layout.ClientLayout;

import java.time.format.DateTimeFormatter;
import java.util. List;
import java.util. Locale;
import java.util. stream.Collectors;

/**
 * Page de gestion des réservations client
 * Route : /client/reservations
 *
 * Fonctionnalités :
 * - Liste de toutes les réservations
 * - Filtrage par statut
 * - Détails de chaque réservation
 * - Annulation (si possible selon règle 48h)
 *
 * Phase 5 :  Utilisateur simulé (ID = 4)
 * Phase 10 : Utilisateur réel via Spring Security
 */
@Route(value = "client/reservations", layout = ClientLayout.class)
@PageTitle("Mes réservations - Festivent")
public class MyReservationsView extends VerticalLayout {

    // TODO Phase 10 : Récupérer l'ID du vrai utilisateur connecté
    private static final Long SIMULATED_USER_ID = 4L; // Client 1 dans DataInit. java

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final ReservationService reservationService;

    private ComboBox<ReservationStatus> statusFilter;
    private VerticalLayout reservationsContainer;

    public MyReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background-color", "var(--festivent-bg)");

        // Header avec titre et filtres
        VerticalLayout header = createHeader();

        // Container des réservations
        reservationsContainer = new VerticalLayout();
        reservationsContainer.setWidthFull();
        reservationsContainer.setSpacing(true);

        add(header, reservationsContainer);

        // Charger les réservations
        loadReservations(null);
    }

    /**
     * Crée le header avec titre et filtres
     */
    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setSpacing(true);

        // Titre
        H2 title = new H2("Mes réservations");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Consultez et gérez vos réservations d'événements");
        subtitle.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("margin", "0 0 var(--lumo-space-m) 0");

        // Filtres
        HorizontalLayout filters = createFilters();

        header.add(title, subtitle, filters);
        return header;
    }

    /**
     * Crée la barre de filtres
     */
    private HorizontalLayout createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(Alignment.CENTER);
        filters.setSpacing(true);

        // Filtre par statut
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter. setClearButtonVisible(true);
        statusFilter.setWidth("250px");

        // Listener pour recharger les réservations
        statusFilter.addValueChangeListener(e -> {
            loadReservations(e.getValue());
        });

        // Bouton rafraîchir
        Button refreshButton = new Button("Rafraîchir", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> {
            loadReservations(statusFilter.getValue());
            showNotification("Liste actualisée", NotificationVariant.LUMO_SUCCESS);
        });

        filters.add(statusFilter, refreshButton);
        return filters;
    }

    /**
     * Charge les réservations avec filtre optionnel
     */
    private void loadReservations(ReservationStatus statusFilter) {
        reservationsContainer.removeAll();

        try {
            // Récupérer toutes les réservations de l'utilisateur
            List<Reservation> allReservations = reservationService. findUserReservations(SIMULATED_USER_ID);

            // Appliquer le filtre si nécessaire
            List<Reservation> filteredReservations = allReservations. stream()
                    .filter(r -> statusFilter == null || r.getStatut() == statusFilter)
                    .sorted((r1, r2) -> r2.getDateReservation().compareTo(r1.getDateReservation())) // Plus récentes en premier
                    .collect(Collectors.toList());

            if (filteredReservations.isEmpty()) {
                // État vide
                VerticalLayout emptyState = createEmptyState();
                reservationsContainer.add(emptyState);
            } else {
                // Afficher les réservations
                for (Reservation reservation : filteredReservations) {
                    VerticalLayout reservationCard = createReservationCard(reservation);
                    reservationsContainer. add(reservationCard);
                }

                // Compteur
                Span counter = new Span(filteredReservations.size() + " réservation(s) trouvée(s)");
                counter.getStyle()
                        .set("color", "var(--festivent-text-tertiary)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("text-align", "center")
                        .set("display", "block")
                        .set("margin-top", "var(--lumo-space-m)");
                reservationsContainer.add(counter);
            }

        } catch (Exception e) {
            Paragraph error = new Paragraph("❌ Erreur lors du chargement des réservations :  " + e.getMessage());
            error.getStyle().set("color", "var(--festivent-error)");
            reservationsContainer. add(error);
        }
    }

    /**
     * Crée une card de réservation détaillée
     */
    private VerticalLayout createReservationCard(Reservation reservation) {
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.setSpacing(true);
        card.setPadding(true);
        card.addClassName("festivent-card");

        // Header de la card (code + statut)
        HorizontalLayout cardHeader = new HorizontalLayout();
        cardHeader.setWidthFull();
        cardHeader. setJustifyContentMode(JustifyContentMode. BETWEEN);
        cardHeader.setAlignItems(Alignment.CENTER);

        Span codeReservation = new Span("Code : " + reservation.getCodeReservation());
        codeReservation.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--festivent-primary)");

        StatusBadge statusBadge = new StatusBadge(reservation. getStatut());

        cardHeader.add(codeReservation, statusBadge);

        // Informations événement
        VerticalLayout eventInfo = new VerticalLayout();
        eventInfo.setSpacing(false);
        eventInfo.setPadding(false);

        H3 eventTitle = new H3(reservation.getEvenement().getTitre());
        eventTitle. getStyle()
                .set("margin", "0 0 var(--lumo-space-s) 0")
                .set("color", "var(--festivent-text-primary)");

        HorizontalLayout dateLayout = createInfoRow(
                VaadinIcon.CALENDAR. create(),
                reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)
        );

        HorizontalLayout lieuLayout = createInfoRow(
                VaadinIcon. LOCATION_ARROW.create(),
                reservation.getEvenement().getLieu() + " - " + reservation.getEvenement().getVille()
        );

        eventInfo.add(eventTitle, dateLayout, lieuLayout);

        // Détails réservation
        HorizontalLayout detailsLayout = new HorizontalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setSpacing(true);
        detailsLayout.getStyle().set("flex-wrap", "wrap");

        Span places = createDetailBadge(
                VaadinIcon.TICKET.create(),
                reservation.getNombrePlaces() + " place(s)"
        );

        Span montant = createDetailBadge(
                VaadinIcon. MONEY.create(),
                reservation.getMontantTotal() + " DH"
        );

        Span dateReservation = createDetailBadge(
                VaadinIcon.CLOCK.create(),
                "Réservé le " + reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH))
        );

        detailsLayout. add(places, montant, dateReservation);

        // Actions
        HorizontalLayout actions = createActionsLayout(reservation);

        card.add(cardHeader, eventInfo, detailsLayout, actions);

        return card;
    }

    /**
     * Crée une ligne d'information (icône + texte)
     */
    private HorizontalLayout createInfoRow(Icon icon, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(Alignment. CENTER);

        icon.setSize("16px");
        icon.getStyle().set("color", "var(--festivent-text-secondary)");

        Span textSpan = new Span(text);
        textSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)");

        row.add(icon, textSpan);
        return row;
    }

    /**
     * Crée un badge de détail (icône + texte)
     */
    private Span createDetailBadge(Icon icon, String text) {
        icon.setSize("16px");
        icon.getStyle().set("margin-right", "var(--lumo-space-xs)");

        Span badge = new Span(icon, new Span(text));
        badge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-primary)");

        return badge;
    }

    /**
     * Crée la barre d'actions (voir événement + annuler)
     */
    private HorizontalLayout createActionsLayout(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode. END);
        actions.setSpacing(true);

        // Bouton voir l'événement
        Button viewEventButton = new Button("Voir l'événement", VaadinIcon.EYE.create());
        viewEventButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        viewEventButton.addClickListener(e -> {
            UI.getCurrent().navigate("event/" + reservation.getEvenement().getId());
        });

        actions.add(viewEventButton);

        // Bouton annuler (si possible)
        if (reservation.isAnnulable()) {
            long heuresRestantes = reservation.getHeuresAvantLimiteAnnulation();

            Button cancelButton = new Button("Annuler", VaadinIcon. CLOSE_CIRCLE.create());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            cancelButton.addClickListener(e -> {
                showCancelConfirmDialog(reservation, heuresRestantes);
            });

            actions.add(cancelButton);
        } else if (reservation.getStatut() == ReservationStatus.CONFIRMEE) {
            // Réservation non annulable (< 48h)
            Span warning = new Span("⚠️ Annulation impossible (< 48h)");
            warning.getStyle()
                    . set("color", "var(--festivent-error)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    . set("font-weight", "600");
            actions.add(warning);
        }

        return actions;
    }

    /**
     * Affiche la dialog de confirmation d'annulation
     */
    private void showCancelConfirmDialog(Reservation reservation, long heuresRestantes) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmer l'annulation");
        dialog.setWidth("500px");

        // Contenu
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        Paragraph message = new Paragraph(
                "Êtes-vous sûr de vouloir annuler cette réservation ?"
        );
        message.getStyle().set("margin", "0");

        Div details = new Div();
        details.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-top", "var(--lumo-space-m)");

        details.add(
                new Paragraph("📅 Événement : " + reservation.getEvenement().getTitre()),
                new Paragraph("🎫 Code : " + reservation.getCodeReservation()),
                new Paragraph("👥 Places : " + reservation.getNombrePlaces()),
                new Paragraph("⏰ Temps restant avant limite : " + heuresRestantes + " heure(s)")
        );

        Paragraph warning = new Paragraph("⚠️ Cette action est irréversible.");
        warning.getStyle()
                .set("color", "var(--festivent-error)")
                .set("font-weight", "600")
                .set("margin-top", "var(--lumo-space-m)");

        content.add(message, details, warning);
        dialog.add(content);

        // Boutons
        Button confirmButton = new Button("Confirmer l'annulation", e -> {
            handleCancelReservation(reservation. getId());
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant. LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> dialog.close());
        cancelButton. addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelButton, confirmButton);

        dialog.open();
    }

    /**
     * Gère l'annulation d'une réservation
     * Utilise ReservationService. annulerReservation() du backend
     */
    private void handleCancelReservation(Long reservationId) {
        try {
            // ✅ UTILISATION DE LA LOGIQUE BACKEND
            reservationService.annulerReservation(reservationId, SIMULATED_USER_ID);

            showNotification("✅ Réservation annulée avec succès", NotificationVariant.LUMO_SUCCESS);

            // Recharger la liste
            loadReservations(statusFilter.getValue());

        } catch (Exception e) {
            showNotification("❌ Erreur :  " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Crée un état vide
     */
    private VerticalLayout createEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setWidthFull();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setPadding(true);
        emptyState.setSpacing(true);

        Icon icon = VaadinIcon.TICKET.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--festivent-text-tertiary)");

        H3 title = new H3("Aucune réservation trouvée");
        title.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("margin", "var(--lumo-space-m) 0 0 0");

        Paragraph message = new Paragraph("Découvrez nos événements et réservez vos places !");
        message.getStyle()
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-align", "center");

        Button exploreButton = new Button("Voir les événements", VaadinIcon. CALENDAR.create());
        exploreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exploreButton.addClickListener(e -> {
            UI.getCurrent().navigate("events");
        });

        emptyState.add(icon, title, message, exploreButton);
        return emptyState;
    }

    /**
     * Affiche une notification
     */
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(variant);
    }
}