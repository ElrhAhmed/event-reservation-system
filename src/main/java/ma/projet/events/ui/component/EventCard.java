package ma.projet.events.ui.component;

import com.vaadin.flow. component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component. button.Button;
import com. vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. html.Div;
import com.vaadin.flow.component. html.H3;
import com.vaadin.flow.component. html.Image;
import com. vaadin.flow.component.html.Span;
import com. vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import ma.projet.events.entity.Event;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
 * card.addDetailsClickListener(e -> UI.getCurrent().navigate("event/" + event. getId()));
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
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH: mm", Locale.FRENCH);

    private static final String DEFAULT_IMAGE = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800";

    /**
     * -- GETTER --
     *  Gets the associated event entity.
     *
     */
    @Getter
    private final Event event;
    private Button viewDetailsButton;

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
                .set("background-color", "white")
                .set("border-radius", "var(--festivent-radius-lg)")
                .set("box-shadow", "var(--festivent-shadow-md)")
                .set("overflow", "hidden")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Hover effect
        getElement().addEventListener("mouseenter", e -> {
            getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "var(--festivent-shadow-xl)");
        });
        getElement().addEventListener("mouseleave", e -> {
            getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "var(--festivent-shadow-md)");
        });

        // Build card sections
        add(createImageSection(), createContentSection(), createBottomSection());
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
                .set("overflow", "hidden");

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
                .set("top", "1rem")
                .set("left", "1rem")
                .set("background-color", "var(--festivent-primary)")
                .set("color", "white")
                .set("padding", "0.25rem 0.75rem")
                .set("border-radius", "1rem")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.15)");

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
                .set("padding", "1rem")
                .set("gap", "0.5rem");

        // Event title
        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.125rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("line-height", "1.3")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("display", "-webkit-box")
                .set("-webkit-line-clamp", "2")
                .set("-webkit-box-orient", "vertical");

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

        // Available seats
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
                .set("font-size", "0.875rem")
                .set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout row = new HorizontalLayout(icon, textSpan);
        row.setAlignItems(FlexComponent. Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle()
                .set("gap", "0.5rem")
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
                .set("padding", "1rem")
                .set("border-top", "1px solid #e5e7eb");

        // ✅ CORRECTION ICI - Formatage sécurisé du prix
        Span price = new Span(formatPrice(event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-primary)");

        // View Details button
        viewDetailsButton = new Button("View Details");
        viewDetailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewDetailsButton.getStyle().set("cursor", "pointer");

        bottom.add(price, viewDetailsButton);
        return bottom;
    }

    /**
     * ✅ NOUVELLE MÉTHODE - Formate le prix de manière sécurisée.
     */
    private String formatPrice(Double price) {
        if (price == null) {
            return "Prix non défini";
        }
        if (price == 0.0) {
            return "Gratuit";
        }
        return String.format("%.2f DH", price);
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

}