package ma.projet.events.ui.component;

import com.vaadin.flow.component. ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component. html. Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin. flow.component.html.Span;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component. orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import ma.projet.events.entity.Event;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Reusable event card component.
 *
 * Displays event information in a visually appealing card format:
 * - Event image with category badge overlay
 * - Title, date, city, available seats
 * - Price and "View Details" button
 *
 * Usage:
 * <pre>
 * Event event = eventService.getEventById(1L);
 * EventCard card = new EventCard(event);
 * card.addDetailsClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));
 * layout.add(card);
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic inside the component
 * - No service/repository calls
 * - Data provided via constructor
 * - Click listener registered externally
 */
public class EventCard extends Div {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.FRENCH);

    private static final String DEFAULT_IMAGE = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800";

    private final Event event;
    private final Button viewDetailsButton;

    /**
     * Creates an event card with the provided event data.
     *
     * @param event The event entity to display
     */
    public EventCard(Event event) {
        this.event = event;

        // Apply global card utility class
        addClassName("festivent-card");

        // Card styling
        getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "320px")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Hover effect
        getElement().addEventListener("mouseenter", e -> {
            getStyle().set("transform", "translateY(-4px)");
        });
        getElement().addEventListener("mouseleave", e -> {
            getStyle().set("transform", "translateY(0)");
        });

        // Build card sections
        add(createImageSection(), createContentSection(), createBottomSection());

        this.viewDetailsButton = (Button) getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .findFirst()
                .map(hl -> ((HorizontalLayout) hl).getChildren()
                        .filter(c -> c instanceof Button)
                        .findFirst()
                        .orElse(null))
                .orElse(null);
    }

    /**
     * Creates the image section with category badge overlay.
     */
    private Div createImageSection() {
        Div imageContainer = new Div();
        imageContainer.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("height", "200px")
                .set("overflow", "hidden")
                .set("border-radius", "var(--festivent-radius-lg) var(--festivent-radius-lg) 0 0");

        // Event image
        String imageUrl = (event.getImageUrl() != null && !event.getImageUrl().isBlank())
                ? event.getImageUrl()
                : DEFAULT_IMAGE;

        Image image = new Image(imageUrl, event.getTitre());
        image.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");

        // Category badge (overlay)
        Span categoryBadge = new Span(
                event.getCategorie().getIcon() + " " + event.getCategorie().getLabel()
        );
        categoryBadge.getStyle()
                .set("position", "absolute")
                .set("top", "var(--festivent-space-md)")
                .set("left", "var(--festivent-space-md)")
                .set("background-color", "var(--festivent-primary)")
                .set("color", "var(--festivent-primary-text)")
                .set("padding", "var(--festivent-space-xs) var(--festivent-space-sm)")
                .set("border-radius", "var(--festivent-radius-xl)")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("box-shadow", "var(--festivent-shadow-md)");

        imageContainer.add(image, categoryBadge);
        return imageContainer;
    }

    /**
     * Creates the content section with title and event details.
     */
    private VerticalLayout createContentSection() {
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        content.getStyle()
                .set("padding", "var(--festivent-space-lg)")
                .set("gap", "var(--festivent-space-sm)");

        // Event title
        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("line-height", "1.3");

        // Date and time
        HorizontalLayout dateInfo = createInfoRow(
                VaadinIcon.CALENDAR_CLOCK,
                event.getDateDebut().format(DATE_FORMATTER)
        );

        // City
        HorizontalLayout cityInfo = createInfoRow(
                VaadinIcon.MAP_MARKER,
                event.getVille()
        );

        // Available seats (placeholder - actual calculation should be done by service)
        HorizontalLayout seatsInfo = createInfoRow(
                VaadinIcon.USERS,
                event.getCapaciteMax() + " places"
        );

        content.add(title, dateInfo, cityInfo, seatsInfo);
        return content;
    }

    /**
     * Creates an info row with icon and text.
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
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle()
                .set("gap", "var(--festivent-space-xs)")
                .set("margin", "0");

        return row;
    }

    /**
     * Creates the bottom section with price and action button.
     */
    private HorizontalLayout createBottomSection() {
        HorizontalLayout bottom = new HorizontalLayout();
        bottom.setWidthFull();
        bottom.setAlignItems(FlexComponent.Alignment.CENTER);
        bottom.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottom.getStyle()
                .set("padding", "0 var(--festivent-space-lg) var(--festivent-space-lg)")
                .set("border-top", "1px solid var(--festivent-secondary)");

        // Price
        Span price = new Span(String.format("%. 2f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-primary)");

        // View Details button
        Button viewDetails = new Button("View Details");
        viewDetails.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewDetails.getStyle().set("cursor", "pointer");

        bottom.add(price, viewDetails);
        return bottom;
    }

    /**
     * Registers a click listener for the "View Details" button.
     *
     * @param listener The listener to invoke when the button is clicked
     */
    public void addDetailsClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        if (viewDetailsButton != null) {
            viewDetailsButton.addClickListener(listener);
        }
    }

    /**
     * Gets the associated event entity.
     *
     * @return The event
     */
    public Event getEvent() {
        return event;
    }
}