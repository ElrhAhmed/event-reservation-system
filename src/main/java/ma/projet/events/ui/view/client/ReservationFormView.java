package ma.projet.events.ui.view.client;

import com.vaadin.flow.component. UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component. button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin. flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet. events.entity.Reservation;
import ma.projet.events.exception.BusinessException;
import ma. projet.events.exception.ResourceNotFoundException;
import ma.projet. events.service.EventService;
import ma.projet. events.service.ReservationService;
import ma.projet.events.ui.component.StatusBadge;
import ma. projet.events.ui.layout.ClientLayout;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Formulaire de réservation d'un événement
 * Route : /client/reserve/{eventId}
 *
 * Fonctionnalités :
 * - Affichage complet de l'événement
 * - Sélection du nombre de places (1-10)
 * - Calcul automatique du montant total
 * - Validation selon règles métier (capacité, statut, etc.)
 * - Confirmation de réservation
 *
 * Phase 5 : Utilisateur simulé (ID = 4)
 * Phase 10 : Utilisateur réel via Spring Security
 */
@Route(value = "client/reserve", layout = ClientLayout.class)
@PageTitle("Réserver - Festivent")
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    // TODO Phase 10 : Récupérer l'ID du vrai utilisateur connecté
    private static final Long SIMULATED_USER_ID = 4L; // Client 1 dans DataInit. java

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm", Locale. FRENCH);

    private final ReservationService reservationService;
    private final EventService eventService;

    private Event currentEvent;
    private IntegerField placesField;
    private Span totalPriceSpan;
    private Button reserveButton;
    private VerticalLayout formContainer;

    public ReservationFormView(ReservationService reservationService, EventService eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        getStyle().set("background-color", "var(--festivent-bg)");
    }

    @Override
    public void setParameter(BeforeEvent event, Long eventId) {
        try {
            // Charger l'événement
            currentEvent = eventService.getEventById(eventId);

            // Créer le contenu
            createContent();

        } catch (ResourceNotFoundException e) {
            showErrorPage("Événement introuvable", "L'événement demandé n'existe pas ou a été supprimé.");
        } catch (Exception e) {
            showErrorPage("Erreur", "Une erreur est survenue :  " + e.getMessage());
        }
    }

    /**
     * Crée le contenu de la page
     */
    private void createContent() {
        removeAll();

        // Bouton retour
        Button backButton = new Button("Retour", VaadinIcon. ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        backButton.addClickListener(e -> {
            UI.getCurrent().navigate("event/" + currentEvent.getId());
        });

        // Titre
        H2 title = new H2("Réserver des places");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        // Layout principal (2 colonnes)
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.getStyle().set("flex-wrap", "wrap");

        // Colonne gauche : Détails événement
        VerticalLayout eventDetails = createEventDetailsSection();
        eventDetails.getStyle()
                .set("flex", "1")
                .set("min-width", "400px");

        // Colonne droite : Formulaire de réservation
        formContainer = createReservationForm();
        formContainer.getStyle()
                .set("flex", "0 0 400px")
                .set("min-width", "300px");

        mainLayout.add(eventDetails, formContainer);

        add(backButton, title, mainLayout);
    }

    /**
     * Crée la section détails de l'événement
     */
    private VerticalLayout createEventDetailsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.addClassName("festivent-card");

        // Header avec catégorie + statut
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode. BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        Span category = new Span(currentEvent.getCategorie().getIcon() + " " + currentEvent.getCategorie().getLabel());
        category.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-primary)");

        StatusBadge statusBadge = new StatusBadge(currentEvent.getStatut());

        header.add(category, statusBadge);

        // Titre événement
        H3 eventTitle = new H3(currentEvent. getTitre());
        eventTitle.getStyle()
                .set("margin", "var(--lumo-space-m) 0")
                .set("color", "var(--festivent-text-primary)");

        // Description
        Paragraph description = new Paragraph(currentEvent.getDescription());
        description.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Informations
        VerticalLayout infos = new VerticalLayout();
        infos.setSpacing(true);
        infos.setPadding(false);

        infos.add(
                createInfoRow(VaadinIcon.CALENDAR. create(), "Date",
                        currentEvent.getDateDebut().format(DATE_FORMATTER)),
                createInfoRow(VaadinIcon.LOCATION_ARROW.create(), "Lieu",
                        currentEvent.getLieu() + " - " + currentEvent. getVille()),
                createInfoRow(VaadinIcon. USERS.create(), "Capacité",
                        getPlacesDisponibles() + " places disponibles sur " + currentEvent.getCapaciteMax()),
                createInfoRow(VaadinIcon.MONEY.create(), "Prix unitaire",
                        currentEvent.getPrixUnitaire() + " DH")
        );

        section.add(header, eventTitle, description, infos);
        return section;
    }

    /**
     * Crée une ligne d'information
     */
    private HorizontalLayout createInfoRow(Icon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.setAlignItems(Alignment.CENTER);

        icon.setSize("20px");
        icon.getStyle().set("color", "var(--festivent-primary)");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setSpacing(false);
        textLayout.setPadding(false);
        textLayout.getStyle().set("flex", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        Span valueSpan = new Span(value);
        valueSpan. getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-text-primary)");

        textLayout.add(labelSpan, valueSpan);
        row.add(icon, textLayout);

        return row;
    }

    /**
     * Crée le formulaire de réservation
     */
    private VerticalLayout createReservationForm() {
        VerticalLayout form = new VerticalLayout();
        form.setSpacing(true);
        form.setPadding(true);
        form.addClassName("festivent-card");

        // Vérifier si l'événement est réservable
        if (!isEventBookable()) {
            form.add(createUnbookableMessage());
            return form;
        }

        // Titre du formulaire
        H3 formTitle = new H3("Réserver vos places");
        formTitle. getStyle()
                .set("margin", "0 0 var(--lumo-space-m) 0")
                .set("color", "var(--festivent-primary)");

        // Champ nombre de places
        placesField = new IntegerField("Nombre de places");
        placesField.setWidthFull();
        placesField.setValue(1);
        placesField.setMin(1);
        placesField.setMax(Math.min(10, getPlacesDisponibles())); // Max 10 ou places disponibles
        placesField.setStepButtonsVisible(true);
        placesField.setHelperText("Maximum :  " + Math.min(10, getPlacesDisponibles()) + " place(s)");

        // Calcul automatique du montant
        placesField.addValueChangeListener(e -> {
            updateTotalPrice();
        });

        // Affichage du montant total
        HorizontalLayout totalLayout = new HorizontalLayout();
        totalLayout.setWidthFull();
        totalLayout.setJustifyContentMode(JustifyContentMode. BETWEEN);
        totalLayout.setAlignItems(Alignment.CENTER);
        totalLayout.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin", "var(--lumo-space-m) 0");

        Span totalLabel = new Span("Montant total");
        totalLabel.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-text-primary)");

        totalPriceSpan = new Span(calculateTotalPrice() + " DH");
        totalPriceSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-accent)");

        totalLayout.add(totalLabel, totalPriceSpan);

        // Bouton de réservation
        reserveButton = new Button("Confirmer la réservation", VaadinIcon.CHECK_CIRCLE.create());
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.setWidthFull();
        reserveButton.addClickListener(e -> handleReservation());

        // Note informative
        Paragraph note = new Paragraph("💡 Vous pourrez annuler votre réservation jusqu'à 48h avant l'événement.");
        note.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("margin", "var(--lumo-space-s) 0 0 0")
                .set("text-align", "center");

        form.add(formTitle, placesField, totalLayout, reserveButton, note);

        return form;
    }

    /**
     * Crée le message pour événement non réservable
     */
    private VerticalLayout createUnbookableMessage() {
        VerticalLayout message = new VerticalLayout();
        message.setAlignItems(Alignment.CENTER);
        message.setSpacing(true);
        message.setPadding(true);

        Icon icon = VaadinIcon.INFO_CIRCLE.create();
        icon.setSize("48px");
        icon.getStyle().set("color", "var(--festivent-error)");

        H3 title = new H3("Réservation impossible");
        title.getStyle()
                .set("color", "var(--festivent-error)")
                .set("margin", "0");

        String reason = getUnbookableReason();
        Paragraph reasonText = new Paragraph(reason);
        reasonText.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("text-align", "center");

        Button backButton = new Button("Voir d'autres événements", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        backButton.addClickListener(e -> {
            UI.getCurrent().navigate("events");
        });

        message.add(icon, title, reasonText, backButton);
        return message;
    }

    /**
     * Vérifie si l'événement est réservable
     */
    private boolean isEventBookable() {
        if (currentEvent. getStatut() != EventStatus.PUBLIE) {
            return false;
        }
        if (currentEvent.getDateDebut().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }
        if (getPlacesDisponibles() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Retourne la raison pour laquelle l'événement n'est pas réservable
     */
    private String getUnbookableReason() {
        if (currentEvent.getStatut() == EventStatus.ANNULE) {
            return "Cet événement a été annulé. ";
        }
        if (currentEvent.getStatut() == EventStatus.TERMINE) {
            return "Cet événement est terminé.";
        }
        if (currentEvent.getStatut() != EventStatus.PUBLIE) {
            return "Cet événement n'est pas encore disponible à la réservation.";
        }
        if (currentEvent.getDateDebut().isBefore(java.time.LocalDateTime.now())) {
            return "Cet événement a déjà commencé.";
        }
        if (getPlacesDisponibles() <= 0) {
            return "Il n'y a plus de places disponibles pour cet événement.";
        }
        return "Réservation impossible pour le moment.";
    }

    /**
     * Calcule le nombre de places disponibles
     */
    private int getPlacesDisponibles() {
        try {
            Integer placesReservees = reservationService.findEventReservations(currentEvent.getId())
                    .stream()
                    .filter(r -> r.getStatut() == ma.projet.events.entity.ReservationStatus.CONFIRMEE)
                    .mapToInt(ma.projet.events.entity. Reservation::getNombrePlaces)
                    . sum();
            return currentEvent.getCapaciteMax() - placesReservees;
        } catch (Exception e) {
            return currentEvent.getCapaciteMax();
        }
    }

    /**
     * Met à jour l'affichage du prix total
     */
    private void updateTotalPrice() {
        totalPriceSpan.setText(calculateTotalPrice() + " DH");
    }

    /**
     * Calcule le prix total
     */
    private double calculateTotalPrice() {
        int places = placesField.getValue() != null ? placesField.getValue() : 1;
        return currentEvent.getPrixUnitaire() * places;
    }

    /**
     * Gère la soumission du formulaire de réservation
     * Utilise ReservationService. reserverTicket() du backend
     */
    private void handleReservation() {
        try {
            // Validation du nombre de places
            if (placesField.getValue() == null || placesField.getValue() < 1) {
                showNotification("❌ Veuillez saisir un nombre de places valide", NotificationVariant.LUMO_ERROR);
                return;
            }

            int nombrePlaces = placesField.getValue();

            // Désactiver le bouton pendant le traitement
            reserveButton. setEnabled(false);
            reserveButton.setText("Réservation en cours...");

            // ✅ UTILISATION DE LA LOGIQUE BACKEND
            Reservation reservation = reservationService.reserverTicket(
                    currentEvent.getId(),
                    SIMULATED_USER_ID,
                    nombrePlaces
            );

            // Succès
            showNotification("✅ Réservation confirmée !  Code :  " + reservation.getCodeReservation(),
                    NotificationVariant.LUMO_SUCCESS);

            // Redirection vers les réservations après 2 secondes
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => { window.location.href = 'client/reservations'; }, 2000);"
            );

        } catch (BusinessException e) {
            // Erreur métier (ex: plus de places, règle non respectée)
            showNotification("❌ " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            reserveButton.setEnabled(true);
            reserveButton.setText("Confirmer la réservation");

        } catch (Exception e) {
            // Erreur technique
            showNotification("❌ Erreur lors de la réservation :  " + e.getMessage(),
                    NotificationVariant. LUMO_ERROR);
            reserveButton.setEnabled(true);
            reserveButton.setText("Confirmer la réservation");
        }
    }

    /**
     * Affiche une page d'erreur
     */
    private void showErrorPage(String title, String message) {
        removeAll();

        VerticalLayout errorLayout = new VerticalLayout();
        errorLayout.setSizeFull();
        errorLayout.setAlignItems(Alignment.CENTER);
        errorLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        Icon icon = VaadinIcon. EXCLAMATION_CIRCLE.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--festivent-error)");

        H2 errorTitle = new H2(title);
        errorTitle.getStyle()
                .set("color", "var(--festivent-error)")
                .set("margin", "var(--lumo-space-m) 0 0 0");

        Paragraph errorMessage = new Paragraph(message);
        errorMessage.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("text-align", "center");

        Button backButton = new Button("Retour aux événements", VaadinIcon. ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addClickListener(e -> {
            UI.getCurrent().navigate("events");
        });

        errorLayout.add(icon, errorTitle, errorMessage, backButton);
        add(errorLayout);
    }

    /**
     * Affiche une notification
     */
    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(variant);
    }
}