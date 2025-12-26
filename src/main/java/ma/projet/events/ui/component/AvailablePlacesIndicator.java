package ma.projet.events.ui.component;

import com.vaadin.flow.component. html.Span;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Small reusable UI component that displays the number of available seats for an event.
 *
 * Features:
 * - Icon + text in horizontal layout
 * - Correct pluralization (seat / seats)
 * - Secondary text color (neutral)
 * - Inline, no background, no border
 *
 * Usage examples:
 * <pre>
 * // In EventCard
 * AvailablePlacesIndicator indicator = new AvailablePlacesIndicator(176);
 * layout.add(indicator);
 *
 * // In EventDetailView
 * int availablePlaces = event.getCapaciteMax() - placesReserved;
 * AvailablePlacesIndicator indicator = new AvailablePlacesIndicator(availablePlaces);
 *
 * // Dynamic update (e.g., after reservation)
 * indicator.updateAvailablePlaces(175);
 *
 * // Zero places
 * AvailablePlacesIndicator soldOut = new AvailablePlacesIndicator(0);
 * // Displays:  "0 seats available"
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Domain-agnostic (just displays a number)
 */
public class AvailablePlacesIndicator extends HorizontalLayout {

    private final Icon icon;
    private final Span textSpan;
    private int availablePlaces;

    /**
     * Creates an available places indicator with the specified number.
     *
     * @param availablePlaces The number of available seats (must be >= 0)
     */
    public AvailablePlacesIndicator(int availablePlaces) {
        this.availablePlaces = Math.max(0, availablePlaces); // Ensure non-negative

        // Container styling
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setPadding(false);
        setMargin(false);
        getStyle()
                .set("gap", "var(--festivent-space-xs)")
                .set("display", "inline-flex");

        // Icon (users/people)
        this.icon = VaadinIcon.USERS.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Text
        this.textSpan = new Span();
        textSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "400")
                .set("line-height", "1");

        updateText();

        add(icon, textSpan);
    }

    /**
     * Updates the displayed number of available places.
     *
     * @param newAvailablePlaces The new number of available seats
     */
    public void updateAvailablePlaces(int newAvailablePlaces) {
        this.availablePlaces = Math. max(0, newAvailablePlaces); // Ensure non-negative
        updateText();
    }

    /**
     * Gets the current number of available places.
     *
     * @return The number of available seats
     */
    public int getAvailablePlaces() {
        return availablePlaces;
    }

    /**
     * Updates the text with correct pluralization.
     */
    private void updateText() {
        String seatText = (availablePlaces == 1) ? "seat" : "seats";
        textSpan.setText(availablePlaces + " " + seatText + " available");
    }

    /**
     * Changes the display text to "X seats left" instead of "X seats available".
     *
     * @param useLeftWording If true, displays "left" instead of "available"
     */
    public void setUseLeftWording(boolean useLeftWording) {
        String seatText = (availablePlaces == 1) ? "seat" : "seats";
        String suffix = useLeftWording ? "left" : "available";
        textSpan.setText(availablePlaces + " " + seatText + " " + suffix);
    }
}