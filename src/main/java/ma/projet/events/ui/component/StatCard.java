package ma.projet.events.ui. component;

import com.vaadin.flow.component.html.Div;
import com. vaadin.flow.component.html. Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.VerticalLayout;

/**
 * Reusable dashboard statistics card component.
 *
 * Displays a metric with:
 * - Label (small, secondary text)
 * - Value (large, bold, primary text)
 * - Icon (in colored container on the right)
 *
 * Usage examples:
 * <pre>
 * StatCard eventsCard = new StatCard("Total Events", "42", VaadinIcon.CALENDAR);
 * StatCard revenueCard = new StatCard("Revenue", "€ 12,450.00", VaadinIcon.EURO);
 * statCard.updateValue("1,234");
 * </pre>
 */
public class StatCard extends Div {

    private final Span labelSpan;
    private final Span valueSpan;
    private final Icon icon;

    /**
     * Creates a statistics card with label, value, and icon.
     *
     * @param label The metric label (e.g., "Total Events")
     * @param value The metric value as String (e.g., "42", "€ 1,234.56")
     * @param iconType The icon to display (from VaadinIcon)
     */
    public StatCard(String label, String value, VaadinIcon iconType) {
        // Apply global card utility class
        addClassName("festivent-card");

        // Card container styling
        getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "var(--festivent-space-lg)")
                .set("min-width", "250px")
                .set("cursor", "default");

        // Left section: label + value
        VerticalLayout leftSection = new VerticalLayout();
        leftSection.setSpacing(false);
        leftSection.setPadding(false);
        leftSection.getStyle().set("gap", "var(--festivent-space-xs)");

        // Label (small, secondary color)
        this.labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin", "0");

        // Value (large, bold, primary color)
        this.valueSpan = new Span(value);
        valueSpan. getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin", "0")
                .set("line-height", "1.2");

        leftSection.add(labelSpan, valueSpan);

        // Right section: icon in colored container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "56px")
                .set("height", "56px")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("background-color", "var(--festivent-accent)")
                .set("flex-shrink", "0");

        this.icon = iconType.create();
        icon.setSize("28px");
        icon.getStyle().set("color", "var(--festivent-accent-text)");

        iconContainer.add(icon);

        // Assemble card
        add(leftSection, iconContainer);
    }

    /**
     * Creates a statistics card with a numeric value.
     *
     * @param label The metric label
     * @param value The metric value as Number (auto-converted to String)
     * @param iconType The icon to display
     */
    public StatCard(String label, Number value, VaadinIcon iconType) {
        this(label, String.valueOf(value), iconType);
    }

    /**
     * Updates the displayed value.
     *
     * @param newValue The new value to display
     */
    public void updateValue(String newValue) {
        valueSpan.setText(newValue != null ? newValue : "0");
    }

    /**
     * Updates the displayed value with a Number.
     *
     * @param newValue The new numeric value
     */
    public void updateValue(Number newValue) {
        updateValue(String.valueOf(newValue));
    }

    /**
     * Gets the current displayed value.
     *
     * @return The current value text
     */
    public String getDisplayValue() {
        return valueSpan.getText();
    }

    /**
     * Updates the label text.
     *
     * @param newLabel The new label
     */
    public void updateLabel(String newLabel) {
        labelSpan.setText(newLabel != null ? newLabel : "");
    }

    /**
     * Gets the current label text.
     *
     * @return The current label
     */
    public String getDisplayLabel() {
        return labelSpan.getText();
    }

    /**
     * Changes the icon.
     *
     * @param newIconType The new icon type
     */
    public void updateIcon(VaadinIcon newIconType) {
        icon.getElement().setAttribute("icon", "vaadin:" + newIconType.name().toLowerCase().replace('_', '-'));
    }
}