package ma.projet.events.ui.view.client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.icon.Icon; // ✅ Import Vaadin icon
import com.vaadin.flow.component.icon.VaadinIcon; // ✅ Import Vaadin icon
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.ui.component.FestiventConfirmDialog;
import ma.projet.events.ui.layout.PublicLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "reservation", layout = PublicLayout.class)
@PageTitle("Reserve Seats - EventReserve")
@AnonymousAllowed
public class ReservationFormView extends VerticalLayout {

    // --- Mocked event data for demonstration ---
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    // Mock event fields
    private final String eventImageUrl =
            "https://images.unsplash.com/photo-1524985069026-dd778a71c7b4?w=800";
    private final String eventTitle = "TEDx Casablanca 2026";
    private final LocalDateTime eventStart = LocalDateTime.now().plusMonths(2).withHour(16).withMinute(0);
    private final LocalDateTime eventEnd = eventStart.plusHours(3);
    private final String eventLocation = "Casablanca City Hall";
    private final double unitPrice = 350.00;
    private final int availableSeats = 96;

    // Form state
    private NumberField numberOfSeatsField;
    private TextArea commentField;
    private Button reserveButton;
    private Span totalPriceValue;
    private HorizontalLayout totalPriceRow;

    public ReservationFormView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f9fafb")
                .set("padding-top", "2rem")
                .set("padding-bottom", "3rem");

        VerticalLayout pageContainer = new VerticalLayout();
        pageContainer.setWidthFull();
        pageContainer.setMaxWidth("1200px");
        pageContainer.setPadding(false);
        pageContainer.setSpacing(false);
        pageContainer.getStyle().set("padding", "0 1.5rem");

        // Top navigation (Back)
        pageContainer.add(createBackLink());

        // Main two-column layout
        HorizontalLayout columns = new HorizontalLayout();
        columns.setWidthFull();
        columns.setSpacing(true);
        columns.setAlignItems(FlexComponent.Alignment.START);
        columns.getStyle().set("gap", "2rem");

        // Left: Event summary
        columns.add(createEventSummaryCard());

        // Right: Reservation Form
        columns.add(createReservationForm());

        columns.setFlexGrow(0, columns.getComponentAt(0)); // Fixed summary
        columns.setFlexGrow(1, columns.getComponentAt(1)); // Flexible form

        pageContainer.add(columns);

        add(pageContainer);
    }

    // --- Components builders ---

    /** Top Back link */
    private HorizontalLayout createBackLink() {
        HorizontalLayout backNav = new HorizontalLayout();
        backNav.setPadding(false);
        backNav.setSpacing(true);
        backNav.setAlignItems(FlexComponent.Alignment.CENTER);
        backNav.getStyle().set("margin-bottom", "2rem").set("cursor", "pointer").set("gap", "0.5rem");

        Icon backIcon = VaadinIcon.ARROW_LEFT.create(); // ✅ Correction ici
        backIcon.setSize("18px");
        backIcon.getStyle().set("color", "#4a5568");

        Span label = new Span("Back to event");
        label.getStyle().set("font-size", "0.95rem").set("color", "#4a5568").set("font-weight", "500");

        backNav.add(backIcon, label);
        backNav.addClickListener(e -> UI.getCurrent().navigate("event/2"));

        backNav.getElement().addEventListener("mouseenter", ev -> {
            backIcon.getStyle().set("color", "#0b64f4");
            label.getStyle().set("color", "#0b64f4");
        });
        backNav.getElement().addEventListener("mouseleave", ev -> {
            backIcon.getStyle().set("color", "#4a5568");
            label.getStyle().set("color", "#4a5568");
        });

        return backNav;
    }

    /** Event Summary (left column) */
    private Div createEventSummaryCard() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        // Image
        Image image = new Image(eventImageUrl, eventTitle);
        image.setWidthFull();
        image.getStyle().set("border-radius", "var(--festivent-radius-lg)").set("height", "180px").set("object-fit", "cover");

        // Title
        H2 title = new H2(eventTitle);
        title.getStyle().set("margin", "0").set("font-size", "1.5rem").set("font-weight", "700").set("color", "#1a202c");

        // Date & time
        Span dateInfo = new Span("🗓 " + eventStart.format(DATE_FMT)
                + " · " + eventStart.format(TIME_FMT) + " - " + eventEnd.format(TIME_FMT));
        dateInfo.getStyle().set("font-size", "0.95rem").set("color", "#0b64f4");

        // Location
        Span locationInfo = new Span("📍 " + eventLocation);
        locationInfo.getStyle().set("font-size", "0.95rem").set("color", "#1a202c");

        content.add(image, title, dateInfo, locationInfo);
        card.add(content);
        return card;
    }

    /** Reservation Form (right column) */
    private Div createReservationForm() {
        Div formCard = new Div();
        formCard.addClassName("festivent-card");
        formCard.setWidth("400px");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);
        form.getStyle().set("gap", "1rem");

        // Title
        H3 formTitle = new H3("Reservation Details");
        formTitle.getStyle().set("margin", "0").set("font-size", "1.175rem").set("font-weight", "700").set("color", "#1a202c");

        // Number of seats
        numberOfSeatsField = new NumberField("Number of seats");
        numberOfSeatsField.setValue(1d);
        numberOfSeatsField.setMin(1);
        numberOfSeatsField.setMax(availableSeats);
        numberOfSeatsField.setStep(1);
        numberOfSeatsField.setStepButtonsVisible(true); // ✅ Correction ici
        numberOfSeatsField.setWidthFull();
        numberOfSeatsField.getStyle().set("font-size", "1rem");
        numberOfSeatsField.addValueChangeListener(e -> updateTotal());

        Span seatsHelper = new Span("You can reserve up to " + availableSeats + " seats.");
        seatsHelper.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "0.25rem");

        // Comment (optional)
        commentField = new TextArea("Comment (optional)");
        commentField.setPlaceholder("Any special requirements...");
        commentField.setWidthFull();
        commentField.setMaxLength(250);

        // Price summary section
        VerticalLayout summarySection = createPriceSummary();

        // Primary action
        reserveButton = new Button("Complete Reservation");
        reserveButton.setWidthFull();
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.setIcon(VaadinIcon.CHECK.create()); // ✅ Correction ici
        reserveButton.addClickListener(e -> handleCompleteReservation());

        form.add(formTitle, numberOfSeatsField, seatsHelper, commentField, summarySection, reserveButton);
        formCard.add(form);

        return formCard;
    }

    /** Price summary (below form fields) */
    private VerticalLayout createPriceSummary() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.6rem").set("margin", "0.5rem 0");

        // Unit price
        HorizontalLayout unitPriceRow = new HorizontalLayout();
        unitPriceRow.setWidthFull();
        unitPriceRow.setAlignItems(FlexComponent.Alignment.CENTER);
        unitPriceRow.getStyle().set("gap", "0.5rem");

        Span unitPriceLabel = new Span("Unit Price:");
        unitPriceLabel.getStyle().set("font-size", "1rem");
        Span unitPriceValue = new Span(formatPrice(unitPrice));
        unitPriceValue.getStyle().set("font-size", "1rem").set("font-weight", "600");

        unitPriceRow.add(unitPriceLabel, unitPriceValue);

        // Subtotal: seats × price
        HorizontalLayout subtotalRow = new HorizontalLayout();
        subtotalRow.setWidthFull();
        subtotalRow.setAlignItems(FlexComponent.Alignment.CENTER);
        subtotalRow.getStyle().set("gap", "0.5rem");
        Span subtotalLabel = new Span("Seats × Price:");
        subtotalLabel.getStyle().set("font-size", "1rem");
        Span subtotalValue = new Span(getSeats() + " × " + formatPrice(unitPrice));
        subtotalValue.getStyle().set("font-size", "1rem");
        subtotalRow.add(subtotalLabel, subtotalValue);

        // Total price (highlighted)
        totalPriceRow = new HorizontalLayout();
        totalPriceRow.setWidthFull();
        totalPriceRow.setAlignItems(FlexComponent.Alignment.CENTER);
        totalPriceRow.getStyle().set("gap", "0.5rem")
                .set("border-top", "1px solid #e5e7eb")
                .set("margin-top", "8px").set("padding-top", "8px");
        Span totalPriceLabel = new Span("Total Price:");
        totalPriceLabel.getStyle().set("font-size", "1.25rem").set("font-weight", "700");
        totalPriceValue = new Span(formatPrice(getSeats() * unitPrice));
        totalPriceValue.getStyle().set("font-size", "1.25rem").set("font-weight", "700").set("color", "#0b64f4");
        totalPriceRow.add(totalPriceLabel, totalPriceValue);

        section.add(unitPriceRow, subtotalRow, totalPriceRow);
        return section;
    }

    // --- Event Handlers ---

    private void handleCompleteReservation() {
        int seats = getSeats();
        if (seats < 1 || seats > availableSeats) {
            Notification.show("Please enter a valid number of seats (1 to " + availableSeats + ").", 3000, Notification.Position.TOP_CENTER);
            return;
        }

        // Prepare the confirmation dialog
        FestiventConfirmDialog dialog = new FestiventConfirmDialog(
                "Confirm Reservation",
                "Please review your reservation details below:",
                "Confirm Reservation"
        );

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle().set("gap", "0.75rem");

        content.add(new Span("Event: " + eventTitle));
        content.add(new Span("Date: " + eventStart.format(DATE_FMT) + " · " + eventStart.format(TIME_FMT)));
        content.add(new Span("Seats Reserved: " + seats));
        content.add(new Span("Total Price: " + formatPrice(seats * unitPrice)));

        dialog.setContent(content);

        dialog.setOnConfirm(() -> {
            Notification.show("Reservation confirmed (mock)!", 3000, Notification.Position.TOP_CENTER);
        });

        dialog.setOnCancel(() -> {
            Notification.show("Reservation cancelled.", 2000, Notification.Position.TOP_CENTER);
        });

        dialog.open();
    }

    // --- Helpers ---

    private int getSeats() {
        Double value = numberOfSeatsField.getValue();
        if (value == null) return 1;
        int v = value.intValue();
        return Math.max(1, Math.min(availableSeats, v));
    }

    private void updateTotal() {
        int seats = getSeats();
        totalPriceValue.setText(formatPrice(seats * unitPrice));
        // Update subtotal text too (for visual consistency)
        if (totalPriceRow.getComponentCount() > 1) {
            // already highlighted section, nothing else needed
        }
    }

    private String formatPrice(double price) {
        return String.format("%.2f DH", price);
    }
}