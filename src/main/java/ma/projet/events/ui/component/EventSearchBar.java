package ma.projet.events.ui. component;

import com.  vaadin.flow.component. ClickEvent;
import com. vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.  button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. combobox.ComboBox;
import com.vaadin.flow.  component.icon.VaadinIcon;
import com. vaadin.flow.component.  orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.textfield.TextField;
import ma.projet.events.entity.EventCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable search bar component for event search.
 *
 * Features:
 * - Text search with icon
 * - Category dropdown filter
 * - Search button (manual trigger)
 *
 * Usage example:
 * <pre>
 * EventSearchBar searchBar = new EventSearchBar();
 *
 * searchBar.  addSearchListener(e -> {
 *     String searchText = searchBar.getSearchText();
 *     EventCategory category = searchBar.  getSelectedCategory();
 *
 *     List<Event> results = eventService.searchWithFilters(
 *         category, null, null, null, null, null, null
 *     );
 *
 *     // Display results
 * });
 *
 * layout. add(searchBar);
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Search triggered only via button click
 * - Data access via getters
 */
public class EventSearchBar extends HorizontalLayout {

    private final TextField searchField;
    private final ComboBox<EventCategory> categoryComboBox;
    private final Button searchButton;

    /**
     * Creates an event search bar with all search controls.
     */
    public EventSearchBar() {
        // Container styling
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        getStyle()
                .set("gap", "var(--festivent-space-sm)")
                .set("padding", "var(--festivent-space-md)")
                .set("background-color", "var(--festivent-surface)")
                .set("border-radius", "var(--festivent-radius-lg)")
                .set("box-shadow", "var(--festivent-shadow-sm)");

        // 1. Search text field
        searchField = new TextField();
        searchField.setPlaceholder("Search events...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.getStyle()
                .set("min-width", "300px");

        // 2. Category selector
        categoryComboBox = new ComboBox<>("Category");
        categoryComboBox. setPlaceholder("All Categories");
        categoryComboBox.setItems(getAllCategories());
        categoryComboBox.setItemLabelGenerator(EventCategory::getLabel);
        categoryComboBox.  setClearButtonVisible(true);
        categoryComboBox.getStyle()
                .set("min-width", "200px");

        // 3. Search button
        searchButton = new Button("Search Events");
        searchButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        searchButton.setIcon(VaadinIcon.SEARCH. create());

        // Allow Enter key to trigger search
        searchField.addKeyPressListener(event -> {
            if (event.getKey().equals("Enter")) {
                searchButton. click();
            }
        });

        add(searchField, categoryComboBox, searchButton);
    }

    /**
     * Gets all available event categories.
     */
    private List<EventCategory> getAllCategories() {
        return Arrays.asList(EventCategory.values());
    }

    /**
     * Gets the current search text.
     *
     * @return The search text (empty string if blank)
     */
    public String getSearchText() {
        String text = searchField.getValue();
        return (text != null && !text.isBlank()) ? text.trim() : "";
    }

    /**
     * Gets the selected category.
     *
     * @return The selected category, or null if "All Categories"
     */
    public EventCategory getSelectedCategory() {
        return categoryComboBox.getValue();
    }

    /**
     * Sets the search text programmatically.
     *
     * @param text The text to set
     */
    public void setSearchText(String text) {
        searchField.setValue(text != null ? text :  "");
    }

    /**
     * Sets the selected category programmatically.
     *
     * @param category The category to select (null for "All Categories")
     */
    public void setSelectedCategory(EventCategory category) {
        categoryComboBox.setValue(category);
    }

    /**
     * Clears all search fields.
     */
    public void clearSearch() {
        searchField.clear();
        categoryComboBox. clear();
    }

    /**
     * Registers a click listener for the search button.
     *
     * @param listener The listener to invoke when search is clicked
     */
    public void addSearchListener(ComponentEventListener<ClickEvent<Button>> listener) {
        searchButton.addClickListener(listener);
    }

    /**
     * Enables or disables the search button.
     *
     * @param enabled true to enable, false to disable
     */
    public void setSearchEnabled(boolean enabled) {
        searchButton.setEnabled(enabled);
    }

    /**
     * Gets the search button (for advanced customization).
     *
     * @return The search button
     */
    public Button getSearchButton() {
        return searchButton;
    }
}