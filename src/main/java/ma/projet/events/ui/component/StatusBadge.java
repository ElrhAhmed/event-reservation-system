package ma.projet.events.ui.component;

import com.vaadin.flow.component. html.Span;

/**
 * Reusable status/category badge component.
 *
 * Displays a compact, pill-shaped badge with customizable text.
 * Designed to work on light backgrounds and overlaid on images.
 *
 * Usage examples:
 * <pre>
 * // Simple badge
 * StatusBadge badge = new StatusBadge("Conference");
 *
 * // Change text dynamically
 * badge.setText("Workshop");
 *
 * // Inside EventCard overlay
 * StatusBadge categoryBadge = new StatusBadge(event.getCategorie().getLabel());
 * imageContainer.add(categoryBadge);
 *
 * // In a grid or layout
 * StatusBadge statusBadge = new StatusBadge(reservation.getStatut().getLabel());
 * grid.addColumn(res -> new StatusBadge(res.getStatut().getLabel()));
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Domain-agnostic (accepts any String)
 * - Styling via global theme variables
 */
public class StatusBadge extends Span {

    /**
     * Creates a status badge with the specified label text.
     *
     * @param text The text to display in the badge (e.g., "Conference", "Published")
     */
    public StatusBadge(String text) {
        setText(text);
        applyStyles();
    }

    /**
     * Creates an empty status badge.
     * Text can be set later using {@link #setText(String)}.
     */
    public StatusBadge() {
        applyStyles();
    }

    /**
     * Applies the badge styling using global theme variables.
     */
    private void applyStyles() {
        // Apply global badge utility class from theme
        addClassName("festivent-badge");
        addClassName("festivent-badge--primary");

        // Inline styles for pill shape and compactness
        getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("padding", "0.25rem 0.75rem")
                .set("border-radius", "var(--festivent-radius-xl)")  // Fully rounded pill shape
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("letter-spacing", "0.025em")
                .set("text-transform", "uppercase")
                .set("white-space", "nowrap")
                .set("background-color", "var(--festivent-primary)")
                .set("color", "var(--festivent-primary-text)")
                .set("box-shadow", "var(--festivent-shadow-sm)");
    }

    /**
     * Updates the badge text.
     *
     * @param text The new text to display
     */
    @Override
    public void setText(String text) {
        super.setText(text != null ? text : "");
    }

    /**
     * Gets the current badge text.
     *
     * @return The badge text
     */
    @Override
    public String getText() {
        return super.getText();
    }
}