package ma.projet.events. ui. component;

import com.vaadin. flow.component. ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component. datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin. flow.component.icon.VaadinIcon;
import com. vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.component. textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ma.projet.events.entity.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time. LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Reusable advanced filter panel - Vertical sidebar style.
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

    public EventFilterPanel() {
        setWidthFull();
        setSpacing(false);
        setPadding(false);
        getStyle().set("gap", "1rem");

        // ✅ Initialize all fields FIRST
        statusComboBox = new ComboBox<>("Status");
        statusComboBox.setWidthFull();
        statusComboBox.setPlaceholder("All status");
        statusComboBox.setItems(getPublicStatuses());
        statusComboBox.setItemLabelGenerator(EventStatus::getLabel);
        statusComboBox.setClearButtonVisible(true);
        statusComboBox.getStyle().set("font-size", "0.875rem");

        cityField = new TextField("City");
        cityField.setWidthFull();
        cityField.setPlaceholder("Enter city");
        cityField.setPrefixComponent(VaadinIcon.MAP_MARKER. create());
        cityField.setClearButtonVisible(true);
        cityField.getStyle().set("font-size", "0.875rem");

        dateMinPicker = new DatePicker();
        dateMinPicker.setWidthFull();
        dateMinPicker.setPlaceholder("From date");
        dateMinPicker.setClearButtonVisible(true);
        dateMinPicker.getStyle().set("font-size", "0.875rem");

        dateMaxPicker = new DatePicker();
        dateMaxPicker.setWidthFull();
        dateMaxPicker.setPlaceholder("To date");
        dateMaxPicker.setClearButtonVisible(true);
        dateMaxPicker.getStyle().set("font-size", "0.875rem");

        priceMinField = new NumberField();
        priceMinField.setWidthFull();
        priceMinField.setPlaceholder("Min price");
        priceMinField. setMin(0);
        priceMinField.setStep(10);
        priceMinField. setClearButtonVisible(true);
        priceMinField.getStyle().set("font-size", "0.875rem");

        priceMaxField = new NumberField();
        priceMaxField.setWidthFull();
        priceMaxField.setPlaceholder("Max price");
        priceMaxField.setMin(0);
        priceMaxField.setStep(10);
        priceMaxField.setClearButtonVisible(true);
        priceMaxField.getStyle().set("font-size", "0.875rem");

        applyButton = new Button("Apply Filters");
        applyButton.setWidthFull();
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.setIcon(VaadinIcon.CHECK.create());

        resetButton = new Button("Reset");
        resetButton. setWidthFull();
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.setIcon(VaadinIcon.REFRESH.create());
        resetButton.addClickListener(e -> resetFilters());

        // ✅ Now build layout
        add(createHeaderSection());
        add(createDivider());
        add(statusComboBox);
        add(cityField);
        add(createDateRangeSection());
        add(createPriceRangeSection());
        add(createDivider());
        add(createActionButtons());
    }

    private HorizontalLayout createHeaderSection() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(false);
        header.setSpacing(false);

        Span title = new Span("Filters");
        title.getStyle()
                .set("font-size", "1rem")
                .set("font-weight", "600")
                .set("color", "#2d3748");

        Button clearLink = new Button("Clear all");
        clearLink.addThemeVariants(ButtonVariant. LUMO_TERTIARY_INLINE);
        clearLink.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#0b64f4")
                .set("cursor", "pointer")
                .set("padding", "0");
        clearLink.addClickListener(e -> resetFilters());

        header.add(title, clearLink);
        return header;
    }

    private Span createDivider() {
        Span divider = new Span();
        divider.getStyle()
                .set("width", "100%")
                .set("height", "1px")
                .set("background-color", "#e2e8f0")
                .set("margin", "0.5rem 0");
        return divider;
    }

    private VerticalLayout createDateRangeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.75rem");

        Span label = new Span("Date Range");
        label.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", "#4a5568")
                .set("display", "block")
                .set("margin-bottom", "0.25rem");

        section.add(label, dateMinPicker, dateMaxPicker);
        return section;
    }

    private VerticalLayout createPriceRangeSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.75rem");

        Span label = new Span("Price Range (DH)");
        label.getStyle()
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", "#4a5568")
                .set("display", "block")
                .set("margin-bottom", "0.25rem");

        section.add(label, priceMinField, priceMaxField);
        return section;
    }

    private VerticalLayout createActionButtons() {
        VerticalLayout actions = new VerticalLayout();
        actions.setWidthFull();
        actions.setPadding(false);
        actions.setSpacing(false);
        actions.getStyle().set("gap", "0.5rem");
        actions.add(applyButton, resetButton);
        return actions;
    }

    private List<EventStatus> getPublicStatuses() {
        return Arrays.asList(
                EventStatus.PUBLIE,
                EventStatus.TERMINE,
                EventStatus.ANNULE
        );
    }

    // ==================== PUBLIC API ====================

    public EventStatus getSelectedStatus() {
        return statusComboBox.getValue();
    }

    public String getCity() {
        String city = cityField.getValue();
        return (city != null && !city.isBlank()) ? city.trim() : null;
    }

    public LocalDateTime getDateMin() {
        LocalDate date = dateMinPicker. getValue();
        return (date != null) ? date.atStartOfDay() : null;
    }

    public LocalDateTime getDateMax() {
        LocalDate date = dateMaxPicker.getValue();
        return (date != null) ? date.atTime(LocalTime.MAX) : null;
    }

    public Double getPriceMin() {
        return priceMinField.getValue();
    }

    public Double getPriceMax() {
        return priceMaxField.getValue();
    }

    public void resetFilters() {
        statusComboBox.clear();
        cityField.clear();
        dateMinPicker.clear();
        dateMaxPicker. clear();
        priceMinField.clear();
        priceMaxField.clear();
    }

    public void addApplyListener(ComponentEventListener<ClickEvent<Button>> listener) {
        applyButton.addClickListener(listener);
    }

    public void addResetListener(ComponentEventListener<ClickEvent<Button>> listener) {
        resetButton.addClickListener(listener);
    }

    public void setFiltersEnabled(boolean enabled) {
        statusComboBox.setEnabled(enabled);
        cityField.setEnabled(enabled);
        dateMinPicker.setEnabled(enabled);
        dateMaxPicker.setEnabled(enabled);
        priceMinField.setEnabled(enabled);
        priceMaxField.setEnabled(enabled);
        applyButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }
}