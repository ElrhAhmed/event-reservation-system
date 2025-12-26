package ma.projet.events.ui. component;

import com.vaadin.flow.component. ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin. flow.component.dialog.Dialog;
import com.vaadin.flow.component.html. Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html. Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import ma.projet.events.entity. Reservation;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Reusable UI component representing a user reservation entry.
 *
 * Features:
 * - Horizontal card layout with reservation details
 * - Status badge (using StatusBadge component)
 * - Action buttons:  View details (eye icon), Cancel (trash icon)
 * - Details modal dialog with full reservation information
 *
 * Usage example:
 * <pre>
 * Reservation reservation = reservationService.getReservationById(1L);
 * ReservationCard card = new ReservationCard(reservation);
 *
 * // Register cancel listener
 * card.addCancelListener(e -> {
 *     reservationService.cancelReservation(reservation.getId());
 *     Notification.show("Reservation cancelled");
 * });
 *
 * layout.add(card);
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Data provided via constructor
 * - Actions handled via listeners
 */
public class ReservationCard extends Div {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.FRENCH);

    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale. FRENCH);

    private final Reservation reservation;
    private final Button cancelButton;

    /**
     * Creates a reservation card with the provided reservation data.
     *
     * @param reservation The reservation entity to display
     */
    public ReservationCard(Reservation reservation) {
        this.reservation = reservation;

        // Apply global card utility class
        addClassName("festivent-card");

        // Card styling
        getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "var(--festivent-space-lg)")
                .set("border", "1px solid var(--festivent-border)")
                .set("margin-bottom", "var(--festivent-space-sm)")
                .set("transition", "all 0.2s ease");

        // Hover effect
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("border-color", "var(--festivent-primary)");
        });
        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("border-color", "var(--festivent-border)");
        });

        // Left section: Reservation details
        VerticalLayout detailsSection = createDetailsSection();

        // Right section: Actions
        HorizontalLayout actionsSection = createActionsSection();

        add(detailsSection, actionsSection);

        this.cancelButton = (Button) actionsSection.getComponentAt(1);
    }

    /**
     * Creates the left section with reservation details.
     */
    private VerticalLayout createDetailsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        section.getStyle().set("gap", "var(--festivent-space-xs)");

        // Row 1: Reservation code + Status badge
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setAlignItems(FlexComponent. Alignment.CENTER);
        row1.setSpacing(true);
        row1.getStyle().set("gap", "var(--festivent-space-sm)");

        Span codeSpan = new Span(reservation. getCodeReservation());
        codeSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "600")
                .set("font-family", "monospace");

        StatusBadge statusBadge = new StatusBadge(reservation. getStatut().getLabel());
        statusBadge.getStyle()
                .set("background-color", reservation.getStatut().getColor())
                .set("color", "white");

        row1.add(codeSpan, statusBadge);

        // Row 2: Event name
        Span eventName = new Span(reservation.getEvenement().getTitre());
        eventName.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Row 3: Event date + Number of seats + Total amount
        HorizontalLayout row3 = new HorizontalLayout();
        row3.setSpacing(true);
        row3.getStyle().set("gap", "var(--festivent-space-lg)");

        // Event date
        Icon calendarIcon = VaadinIcon.CALENDAR. create();
        calendarIcon. setSize("14px");
        calendarIcon. getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span dateSpan = new Span(reservation.getEvenement().getDateDebut().format(SIMPLE_DATE_FORMATTER));
        dateSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout dateInfo = new HorizontalLayout(calendarIcon, dateSpan);
        dateInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        dateInfo.setSpacing(true);
        dateInfo.getStyle().set("gap", "var(--festivent-space-xs)");

        // Number of seats
        Icon seatsIcon = VaadinIcon.TICKET.create();
        seatsIcon.setSize("14px");
        seatsIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        String seatsText = reservation.getNombrePlaces() + " place" + (reservation.getNombrePlaces() > 1 ? "s" : "");
        Span seatsSpan = new Span(seatsText);
        seatsSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout seatsInfo = new HorizontalLayout(seatsIcon, seatsSpan);
        seatsInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        seatsInfo.setSpacing(true);
        seatsInfo.getStyle().set("gap", "var(--festivent-space-xs)");

        // Total amount
        Span amountSpan = new Span(String.format("%. 2f DH", reservation.getMontantTotal()));
        amountSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-primary)");

        row3.add(dateInfo, seatsInfo, amountSpan);

        section.add(row1, eventName, row3);
        return section;
    }

    /**
     * Creates the right section with action buttons.
     */
    private HorizontalLayout createActionsSection() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("gap", "var(--festivent-space-xs)");

        // View details button
        Button viewButton = new Button(VaadinIcon.EYE. create());
        viewButton.addThemeVariants(ButtonVariant. LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        viewButton.getStyle().set("color", "var(--festivent-primary)");
        viewButton.addClickListener(e -> openDetailsDialog());

        // Cancel button
        Button cancelBtn = new Button(VaadinIcon.TRASH.create());
        cancelBtn. addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        cancelBtn.getStyle().set("color", "var(--festivent-error)");

        // Disable cancel button if not cancellable
        if (!reservation. isAnnulable() || reservation.isAnnulee()) {
            cancelBtn. setEnabled(false);
            cancelBtn.getStyle().set("color", "var(--lumo-disabled-text-color)");
        }

        actions.add(viewButton, cancelBtn);
        return actions;
    }

    /**
     * Opens a modal dialog with full reservation details.
     */
    private void openDetailsDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.setAlignItems(FlexComponent. Alignment.CENTER);

        H3 title = new H3("Détails de la réservation");
        title.getStyle().set("margin", "0");

        Button closeButton = new Button(VaadinIcon.CLOSE.create());
        closeButton.addThemeVariants(ButtonVariant. LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> dialog.close());

        header.add(title, closeButton);

        // Content:  Two-column layout
        Div content = new Div();
        content.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "var(--festivent-space-md)")
                .set("padding", "var(--festivent-space-lg)");

        content.add(
                createDetailRow("Code de réservation", reservation.getCodeReservation()),
                createDetailRow("Statut", createStatusBadgeForDialog()),
                createDetailRow("Événement", reservation.getEvenement().getTitre()),
                createDetailRow("Date de l'événement", reservation.getEvenement().getDateDebut().format(DATE_FORMATTER)),
                createDetailRow("Nombre de places", String.valueOf(reservation.getNombrePlaces())),
                createDetailRow("Prix unitaire", String.format("%.2f DH", reservation.getPrixUnitaire())),
                createDetailRow("Montant total", String.format("%.2f DH", reservation.getMontantTotal())),
                createDetailRow("Date de réservation", reservation.getDateReservation().format(DATE_FORMATTER))
        );

        // Add comment if exists
        if (reservation.getCommentaire() != null && !reservation.getCommentaire().isBlank()) {
            Div commentSection = new Div();
            commentSection.getStyle().set("grid-column", "1 / -1");

            Span commentLabel = new Span("Commentaire");
            commentLabel.getStyle()
                    .set("font-weight", "600")
                    .set("color", "var(--festivent-secondary-text)")
                    .set("display", "block")
                    .set("margin-bottom", "var(--festivent-space-xs)");

            Span commentText = new Span(reservation.getCommentaire());
            commentText.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-style", "italic");

            commentSection.add(commentLabel, commentText);
            content.add(commentSection);
        }

        VerticalLayout dialogLayout = new VerticalLayout(header, content);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);

        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Creates a detail row for the dialog (label + value).
     */
    private Div createDetailRow(String label, String value) {
        Div row = new Div();

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("display", "block")
                .set("margin-bottom", "var(--festivent-space-xs)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        row.add(labelSpan, valueSpan);
        return row;
    }

    /**
     * Creates a detail row with a component as value (for status badge).
     */
    private Div createDetailRow(String label, Div valueComponent) {
        Div row = new Div();

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("display", "block")
                .set("margin-bottom", "var(--festivent-space-xs)");

        row.add(labelSpan, valueComponent);
        return row;
    }

    /**
     * Creates a status badge for the details dialog.
     */
    private Div createStatusBadgeForDialog() {
        StatusBadge badge = new StatusBadge(reservation.getStatut().getLabel());
        badge.getStyle()
                .set("background-color", reservation.getStatut().getColor())
                .set("color", "white");

        Div container = new Div(badge);
        return container;
    }

    /**
     * Registers a click listener for the cancel button.
     *
     * @param listener The listener to invoke when cancel is clicked
     */
    public void addCancelListener(ComponentEventListener<ClickEvent<Button>> listener) {
        if (cancelButton != null) {
            cancelButton.addClickListener(listener);
        }
    }

    /**
     * Gets the associated reservation entity.
     *
     * @return The reservation
     */
    public Reservation getReservation() {
        return reservation;
    }

    /**
     * Checks if the cancel button is enabled.
     *
     * @return true if cancellable, false otherwise
     */
    public boolean isCancellable() {
        return reservation.isAnnulable() && !reservation.isAnnulee();
    }
}