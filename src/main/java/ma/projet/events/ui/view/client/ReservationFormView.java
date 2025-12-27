package ma.projet.events.ui.view. client;

import com.vaadin.flow.component.UI;
import com. vaadin.flow. component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin. flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component. notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.component. textfield.IntegerField;
import com.vaadin. flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import jakarta.annotation.security. PermitAll;
import ma.projet.events. entity.Event;
import ma.projet. events.entity. Reservation;
import ma.projet.events. entity.User;
import ma. projet.events.exception.BusinessException;
import ma.projet.events. exception.ConflictException;
import ma.projet. events.exception.ResourceNotFoundException;
import ma. projet.events.security.SecurityService;
import ma. projet.events.service.EventService;
import ma.projet.events. service.ReservationService;
import ma. projet.events.ui.layout.PublicLayout;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Formulaire de réservation d'un événement.
 * Route:  /reservation/{eventId}
 */
@Route(value = "reservation", layout = PublicLayout.class)
@PageTitle("Reserve Seats - EventReserve")
@PermitAll
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final SecurityService securityService;
    private final EventService eventService;
    private final ReservationService reservationService;

    // Données
    private Long eventId;
    private Event event;
    private User currentUser;
    private int availableSeats;

    // Composants UI
    private IntegerField numberOfSeatsField;
    private TextArea commentField;
    private Span seatsValueSpan;
    private Span totalPriceSpan;
    private Button reserveButton;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale. ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale. ENGLISH);
    private static final String DEFAULT_IMAGE =
            "https://images.unsplash. com/photo-1492684223066-81342ee5ff30?w=800";

    public ReservationFormView(SecurityService securityService,
                               EventService eventService,
                               ReservationService reservationService) {
        this.securityService = securityService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding-top", "2rem")
                .set("padding-bottom", "3rem");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Long eventId) {
        // Vérifier l'authentification
        if (! securityService.isUserLoggedIn()) {
            beforeEvent.rerouteTo("login");
            return;
        }

        this.currentUser = securityService.getAuthenticatedUser();

        if (eventId == null) {
            showError("Event ID is missing");
            beforeEvent.rerouteTo("events");
            return;
        }

        try {
            this. eventId = eventId;
            this. event = eventService. getEventById(eventId);
            this.availableSeats = eventService.calculateAvailablePlaces(eventId);

            // Vérifier si l'utilisateur a déjà réservé
            if (reservationService.hasUserReservedEvent(currentUser. getId(), eventId)) {
                Notification.show("You already have a reservation for this event",
                                4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_WARNING);
                beforeEvent.forwardTo("event/" + eventId);
                return;
            }

            // Construire l'interface
            buildUI();

        } catch (ResourceNotFoundException e) {
            showError("Event not found");
            beforeEvent.rerouteTo("events");
        }
    }

    /**
     * Construit l'interface utilisateur
     */
    private void buildUI() {
        removeAll();

        VerticalLayout pageContainer = new VerticalLayout();
        pageContainer.setWidthFull();
        pageContainer.setMaxWidth("1000px");
        pageContainer.setPadding(false);
        pageContainer. setSpacing(false);
        pageContainer.getStyle().set("padding", "0 1. 5rem");

        // Back link
        pageContainer.add(createBackLink());

        // Page title
        pageContainer.add(createPageTitle());

        // Main content:  2 columns
        HorizontalLayout columns = new HorizontalLayout();
        columns.setWidthFull();
        columns.setSpacing(true);
        columns.setAlignItems(FlexComponent.Alignment.START);
        columns.getStyle().set("gap", "2rem");

        columns.add(createEventSummaryCard());
        columns.add(createReservationForm());

        pageContainer.add(columns);
        add(pageContainer);
    }

    /**
     * Lien retour
     */
    private HorizontalLayout createBackLink() {
        HorizontalLayout backNav = new HorizontalLayout();
        backNav.setPadding(false);
        backNav. setSpacing(true);
        backNav.setAlignItems(FlexComponent.Alignment. CENTER);
        backNav.getStyle()
                .set("margin-bottom", "1. 5rem")
                .set("cursor", "pointer")
                .set("gap", "0.5rem");

        Icon backIcon = VaadinIcon. ARROW_LEFT. create();
        backIcon.setSize("18px");
        backIcon. getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span label = new Span("Back");
        label.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500");

        backNav.add(backIcon, label);
        backNav.addClickListener(e -> UI.getCurrent().navigate("event/" + eventId));

        // Hover effect
        backNav.getElement().addEventListener("mouseenter", ev -> {
            backIcon.getStyle().set("color", "var(--festivent-primary)");
            label. getStyle().set("color", "var(--festivent-primary)");
        });
        backNav.getElement().addEventListener("mouseleave", ev -> {
            backIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");
            label.getStyle().set("color", "var(--lumo-secondary-text-color)");
        });

        return backNav;
    }

    /**
     * Titre de la page
     */
    private H2 createPageTitle() {
        H2 title = new H2("Complete Your Reservation");
        title.getStyle()
                .set("margin", "0 0 2rem 0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");
        return title;
    }

    /**
     * Carte résumé de l'événement (colonne gauche)
     */
    private Div createEventSummaryCard() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.setWidth("400px");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.getStyle().set("gap", "var(--festivent-space-md)");

        // Section title
        H3 sectionTitle = new H3("Event Summary");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Image
        String imageUrl = (event.getImageUrl() != null && !event.getImageUrl().isBlank())
                ? event. getImageUrl() : DEFAULT_IMAGE;
        Image image = new Image(imageUrl, event.getTitre());
        image.setWidthFull();
        image.getStyle()
                .set("border-radius", "var(--festivent-radius-md)")
                .set("height", "180px")
                .set("object-fit", "cover");

        // Event title
        H3 eventTitle = new H3(event. getTitre());
        eventTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Date & Time
        HorizontalLayout dateInfo = createInfoRow(
                VaadinIcon. CALENDAR,
                event.getDateDebut().format(DATE_FORMATTER) + " • " + event.getDateDebut().format(TIME_FORMATTER)
        );

        // Location
        HorizontalLayout locationInfo = createInfoRow(
                VaadinIcon. MAP_MARKER,
                event.getLieu() + ", " + event.getVille()
        );

        content.add(sectionTitle, image, eventTitle, dateInfo, locationInfo);
        card.add(content);
        return card;
    }

    /**
     * Crée une ligne d'info avec icône
     */
    private HorizontalLayout createInfoRow(VaadinIcon iconType, String text) {
        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span textSpan = new Span(text);
        textSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout row = new HorizontalLayout(icon, textSpan);
        row.setAlignItems(FlexComponent.Alignment. CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "0.5rem");

        return row;
    }

    /**
     * Formulaire de réservation (colonne droite)
     */
    private Div createReservationForm() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("flex", "1")
                .set("padding", "var(--festivent-space-lg)");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);
        form.getStyle().set("gap", "var(--festivent-space-md)");

        // Section title
        H3 sectionTitle = new H3("Reservation Details");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Number of seats
        numberOfSeatsField = new IntegerField("Number of Seats");
        numberOfSeatsField.setValue(1);
        numberOfSeatsField.setMin(1);
        numberOfSeatsField. setMax(Math.min(10, availableSeats));
        numberOfSeatsField.setStepButtonsVisible(true);
        numberOfSeatsField.setWidthFull();
        numberOfSeatsField.addValueChangeListener(e -> updatePriceSummary());

        // Helper text
        Span seatsHelper = new Span("Max 10 seats • " + availableSeats + " available");
        seatsHelper. getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Comment field
        commentField = new TextArea("Comment (optional)");
        commentField.setPlaceholder("Any special requirements.. .");
        commentField.setWidthFull();
        commentField.setMaxLength(250);

        // Price summary
        VerticalLayout priceSummary = createPriceSummary();

        // Reserve button
        reserveButton = new Button("Complete Reservation");
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant. LUMO_LARGE);
        reserveButton.setWidthFull();
        reserveButton.addClickListener(e -> handleReservation());

        form.add(sectionTitle, numberOfSeatsField, seatsHelper, commentField, priceSummary, reserveButton);
        card.add(form);
        return card;
    }

    /**
     * Résumé des prix
     */
    private VerticalLayout createPriceSummary() {
        VerticalLayout summary = new VerticalLayout();
        summary.setPadding(false);
        summary.setSpacing(false);
        summary.getStyle()
                .set("gap", "var(--festivent-space-sm)")
                .set("margin-top", "var(--festivent-space-md)");

        // Unit price row
        HorizontalLayout unitPriceRow = createPriceRow("Unit price", formatPrice(event.getPrixUnitaire()));

        // Seats row
        seatsValueSpan = new Span("×1");
        seatsValueSpan.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "600");
        HorizontalLayout seatsRow = new HorizontalLayout();
        seatsRow.setWidthFull();
        seatsRow. setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        Span seatsLabel = new Span("Seats");
        seatsLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        seatsRow.add(seatsLabel, seatsValueSpan);

        // Divider
        Hr divider = new Hr();
        divider.getStyle().set("margin", "var(--festivent-space-sm) 0");

        // Total row
        totalPriceSpan = new Span(formatPrice(event. getPrixUnitaire()));
        totalPriceSpan.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-l)");

        HorizontalLayout totalRow = new HorizontalLayout();
        totalRow.setWidthFull();
        totalRow.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        totalRow.setAlignItems(FlexComponent.Alignment.CENTER);

        Span totalLabel = new Span("Total");
        totalLabel.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "var(--festivent-secondary-text)");

        totalRow.add(totalLabel, totalPriceSpan);

        summary.add(unitPriceRow, seatsRow, divider, totalRow);
        return summary;
    }

    /**
     * Crée une ligne de prix
     */
    private HorizontalLayout createPriceRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "var(--festivent-secondary-text)");

        row.add(labelSpan, valueSpan);
        return row;
    }

    /**
     * Met à jour le résumé des prix
     */
    private void updatePriceSummary() {
        int seats = getSeatsValue();
        seatsValueSpan. setText("×" + seats);
        double total = event.getPrixUnitaire() * seats;
        totalPriceSpan.setText(formatPrice(total));
    }

    /**
     * Gère la réservation
     */
    private void handleReservation() {
        int seats = getSeatsValue();

        if (seats < 1 || seats > Math.min(10, availableSeats)) {
            Notification.show("Please enter a valid number of seats (1-" + Math.min(10, availableSeats) + ")",
                            3000, Notification. Position.TOP_CENTER)
                    . addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Afficher le dialog de confirmation
        showConfirmationDialog(seats);
    }

    /**
     * Dialog de confirmation
     */
    private void showConfirmationDialog(int seats) {
        Dialog dialog = new Dialog();
        dialog.setWidth("450px");
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment. STRETCH);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.setAlignItems(FlexComponent. Alignment.CENTER);

        H3 title = new H3("Confirm Reservation");
        title.getStyle().set("margin", "0");

        Button closeButton = new Button(VaadinIcon. CLOSE. create());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant. LUMO_ICON);
        closeButton.addClickListener(e -> dialog.close());

        header.add(title, closeButton);

        // Subtitle
        Span subtitle = new Span("Please review your reservation details before confirming.");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Details grid
        Div detailsGrid = new Div();
        detailsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "var(--festivent-space-md)")
                .set("margin", "var(--festivent-space-lg) 0");

        detailsGrid.add(
                createDetailItem("Event", event.getTitre()),
                createDetailItem("Date", event.getDateDebut().format(DATE_FORMATTER)),
                createDetailItem("Seats", String.valueOf(seats)),
                createDetailItem("Total", formatPrice(event.getPrixUnitaire() * seats))
        );

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode. END);
        buttons.setSpacing(true);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmButton = new Button("Confirm Reservation", e -> {
            dialog.close();
            processReservation(seats);
        });
        confirmButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY);

        buttons.add(cancelButton, confirmButton);

        content.add(header, subtitle, detailsGrid, buttons);
        dialog.add(content);
        dialog.open();
    }

    /**
     * Crée un item de détail pour le dialog
     */
    private Div createDetailItem(String label, String value) {
        Div item = new Div();

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--festivent-primary)")
                .set("margin-bottom", "4px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        item.add(labelSpan, valueSpan);
        return item;
    }

    /**
     * Traite la réservation
     */
    private void processReservation(int seats) {
        try {
            Reservation reservation = reservationService.reserverTicket(
                    eventId,
                    currentUser.getId(),
                    seats
            );

            // Afficher le dialog de succès
            showSuccessDialog(reservation. getCodeReservation());

        } catch (BusinessException | ConflictException e) {
            Notification.show(e.getMessage(), 4000, Notification. Position.TOP_CENTER)
                    . addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Dialog de succès
     */
    private void showSuccessDialog(String reservationCode) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent. Alignment.CENTER);

        // Close button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        closeButton. addClickListener(e -> {
            dialog.close();
            UI.getCurrent().navigate("client/reservations");
        });
        header.add(closeButton);

        // Success icon
        Icon checkIcon = VaadinIcon.CHECK_CIRCLE.create();
        checkIcon. setSize("64px");
        checkIcon.getStyle().set("color", "#22c55e");

        // Title
        H3 title = new H3("Reservation Confirmed!");
        title.getStyle()
                .set("margin", "0")
                .set("color", "var(--festivent-secondary-text)");

        // Subtitle
        Span subtitle = new Span("Your reservation has been successfully created.");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center");

        // Reservation code
        Span codeLabel = new Span("Your reservation code");
        codeLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-top", "var(--festivent-space-md)");

        Span codeValue = new Span(reservationCode);
        codeValue.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("font-family", "monospace")
                .set("letter-spacing", "0.05em");

        // Buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.getStyle().set("margin-top", "var(--festivent-space-lg)");

        Button viewReservationsButton = new Button("View My Reservations", e -> {
            dialog.close();
            UI.getCurrent().navigate("client/reservations");
        });
        viewReservationsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewReservationsButton. getStyle()
                .set("border", "1px solid var(--festivent-secondary)");

        Button browseEventsButton = new Button("Browse More Events", e -> {
            dialog.close();
            UI.getCurrent().navigate("events");
        });
        browseEventsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        buttons.add(viewReservationsButton, browseEventsButton);

        content.add(header, checkIcon, title, subtitle, codeLabel, codeValue, buttons);
        dialog.add(content);
        dialog.open();
    }

    /**
     * Récupère le nombre de places
     */
    private int getSeatsValue() {
        Integer value = numberOfSeatsField.getValue();
        if (value == null) return 1;
        return Math.max(1, Math.min(value, Math.min(10, availableSeats)));
    }

    /**
     * Formate le prix
     */
    private String formatPrice(Double price) {
        if (price == null) return "€0.00";
        return String.format("€%.2f", price);
    }

    /**
     * Affiche une erreur
     */
    private void showError(String message) {
        Notification. show(message, 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}