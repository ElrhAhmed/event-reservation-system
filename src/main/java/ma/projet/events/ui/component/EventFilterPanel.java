package ma.projet.  events.ui.component;

import com. vaadin.flow.component. ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.  vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. combobox.ComboBox;
import com.vaadin.  flow.component.datepicker.DatePicker;
import com.vaadin.flow. component.html.Span;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield. TextField;
import ma.projet.events.entity.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util. Arrays;
import java.util. List;

/**
 * Reusable advanced filter panel for event listing.
 *
 * Features:
 * - Status filter (Upcoming, Ongoing, Past, Cancelled)
 * - Date range filter
 * - Price range filter
 * - City/Location filter
 * - Apply/Reset buttons
 *
 * Usage example:
 * <pre>
 * EventFilterPanel filterPanel = new EventFilterPanel();
 *
 * filterPanel. addApplyListener(e -> {
 *     EventStatus status = filterPanel.getSelectedStatus();
 *     String city = filterPanel.getCity();
 *     LocalDateTime dateMin = filterPanel.getDateMin();
 *     LocalDateTime dateMax = filterPanel.getDateMax();
 *     Double priceMin = filterPanel.getPriceMin();
 *     Double priceMax = filterPanel.getPriceMax();
 *
 *     List<Event> results = eventService.searchWithFilters(
 *         null, city, dateMin, dateMax, null, priceMin, priceMax
 *     );
 *
 *     // Display filtered results
 * });
 *
 * layout.add(filterPanel);
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Filters applied only via button click
 * - Data access via getters
 */
public class EventFilterPanel extends VerticalLayout {

    private final ComboBox<EventStatus> statusComboBox;
    private final TextField cityField;
    private final DatePicker dateMinPicker;
    private final DatePicker dateMaxPicker;
    private final NumberField priceMinField;
    private final NumberField priceMaxField;
    private final Button applyButton;
    private final Button resetButton;

    /**
     * Creates an event filter panel with all filter controls.
     */
    public EventFilterPanel() {
        // Container styling
        setWidthFull();
        setSpacing(true);
        setPadding(true);
        getStyle()
                .set("background-color", "var(--festivent-surface)")
                .set("border-radius", "var(--festivent-radius-lg)")
                .set("box-shadow", "var(--festivent-shadow-sm)")
                .set("padding", "var(--festivent-space-lg)");

        // Header
        Span header = new Span("Advanced Filters");
        header.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin-bottom", "var(--festivent-space-sm)");

        add(header);

        // Row 1: Status + City
        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.setSpacing(true);
        row1.  getStyle().set("gap", "var(--festivent-space-sm)");

        // Status filter
        statusComboBox = new ComboBox<>("Status");
        statusComboBox.setPlaceholder("All Status");
        statusComboBox. setItems(getPublicStatuses());
        statusComboBox.setItemLabelGenerator(EventStatus::getLabel);
        statusComboBox.setClearButtonVisible(true);
        statusComboBox.setWidthFull();

        // City filter
        cityField = new TextField("City");
        cityField.setPlaceholder("Enter city name");
        cityField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        cityField.setClearButtonVisible(true);
        cityField.setWidthFull();

        row1.add(statusComboBox, cityField);

        // Row 2: Date range
        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.setSpacing(true);
        row2.getStyle().set("gap", "var(--festivent-space-sm)");

        dateMinPicker = new DatePicker("From Date");
        dateMinPicker. setPlaceholder("Select start date");
        dateMinPicker.  setClearButtonVisible(true);
        dateMinPicker.setWidthFull();

        dateMaxPicker = new DatePicker("To Date");
        dateMaxPicker.setPlaceholder("Select end date");
        dateMaxPicker. setClearButtonVisible(true);
        dateMaxPicker.setWidthFull();

        row2.add(dateMinPicker, dateMaxPicker);

        // Row 3: Price range
        HorizontalLayout row3 = new HorizontalLayout();
        row3.setWidthFull();
        row3.setSpacing(true);
        row3.getStyle().set("gap", "var(--festivent-space-sm)");

        priceMinField = new NumberField("Min Price (DH)");
        priceMinField.setPlaceholder("0");
        priceMinField. setMin(0);
        priceMinField.setStep(10);
        priceMinField. setClearButtonVisible(true);
        priceMinField.setWidthFull();

        priceMaxField = new NumberField("Max Price (DH)");
        priceMaxField.setPlaceholder("10000");
        priceMaxField.setMin(0);
        priceMaxField.setStep(10);
        priceMaxField.setClearButtonVisible(true);
        priceMaxField.setWidthFull();

        row3.add(priceMinField, priceMaxField);

        // Action buttons
        HorizontalLayout actionsRow = new HorizontalLayout();
        actionsRow.setWidthFull();
        actionsRow.setJustifyContentMode(JustifyContentMode.END);
        actionsRow.setSpacing(true);
        actionsRow.getStyle().set("gap", "var(--festivent-space-sm)");

        resetButton = new Button("Reset", VaadinIcon.REFRESH. create());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> resetFilters());

        applyButton = new Button("Apply Filters", VaadinIcon. FILTER. create());
        applyButton. addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        actionsRow.add(resetButton, applyButton);

        add(row1, row2, row3, actionsRow);
    }

    /**
     * Gets only public-facing statuses (excludes BROUILLON).
     */
    private List<EventStatus> getPublicStatuses() {
        return Arrays.asList(
                EventStatus. PUBLIE,
                EventStatus.TERMINE,
                EventStatus.ANNULE
        );
    }

    /**
     * Gets the selected status.
     *
     * @return The selected status, or null if "All Status"
     */
    public EventStatus getSelectedStatus() {
        return statusComboBox.getValue();
    }

    /**
     * Gets the city filter value.
     *
     * @return The city name, or null if empty
     */
    public String getCity() {
        String city = cityField.getValue();
        return (city != null && ! city.isBlank()) ? city.trim() : null;
    }

    /**
     * Gets the minimum date (converted to LocalDateTime at start of day).
     *
     * @return The minimum date, or null if not set
     */
    public LocalDateTime getDateMin() {
        LocalDate date = dateMinPicker. getValue();
        return (date != null) ? date.atStartOfDay() : null;
    }

    /**
     * Gets the maximum date (converted to LocalDateTime at end of day).
     *
     * @return The maximum date, or null if not set
     */
    public LocalDateTime getDateMax() {
        LocalDate date = dateMaxPicker. getValue();
        return (date != null) ? date.atTime(LocalTime.MAX) : null;
    }

    /**
     * Gets the minimum price.
     *
     * @return The minimum price, or null if not set
     */
    public Double getPriceMin() {
        return priceMinField.getValue();
    }

    /**
     * Gets the maximum price.
     *
     * @return The maximum price, or null if not set
     */
    public Double getPriceMax() {
        return priceMaxField.getValue();
    }

    /**
     * Resets all filters to default values.
     */
    public void resetFilters() {
        statusComboBox.clear();
        cityField.clear();
        dateMinPicker.clear();
        dateMaxPicker. clear();
        priceMinField.clear();
        priceMaxField.clear();
    }

    /**
     * Registers a click listener for the apply button.
     *
     * @param listener The listener to invoke when apply is clicked
     */
    public void addApplyListener(ComponentEventListener<ClickEvent<Button>> listener) {
        applyButton.addClickListener(listener);
    }

    /**
     * Registers a click listener for the reset button.
     *
     * @param listener The listener to invoke when reset is clicked
     */
    public void addResetListener(ComponentEventListener<ClickEvent<Button>> listener) {
        resetButton.addClickListener(listener);
    }

    /**
     * Enables or disables all filter controls.
     *
     * @param enabled true to enable, false to disable
     */
    public void setFiltersEnabled(boolean enabled) {
        statusComboBox.setEnabled(enabled);
        cityField. setEnabled(enabled);
        dateMinPicker.setEnabled(enabled);
        dateMaxPicker. setEnabled(enabled);
        priceMinField.setEnabled(enabled);
        priceMaxField. setEnabled(enabled);
        applyButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }
}