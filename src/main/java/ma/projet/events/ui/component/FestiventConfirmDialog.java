package ma.projet.events.ui.component;

import com.vaadin.flow.component. Component;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. dialog.Dialog;
import com. vaadin.flow.component.html. Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin. flow.component.html.Span;
import com.vaadin. flow.component.icon.VaadinIcon;
import com. vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Reusable confirmation dialog component.
 *
 * Features:
 * - Centered modal with overlay
 * - Customizable title and description
 * - Flexible content area (inject any component)
 * - Confirm/Cancel actions with callbacks
 * - Close button (X) at top-right
 *
 * Usage examples:
 * <pre>
 * // Simple confirmation
 * FestiventConfirmDialog dialog = new FestiventConfirmDialog(
 *     "Confirm Reservation",
 *     "Are you sure you want to reserve 2 seats for this event?",
 *     "Confirm Reservation"
 * );
 *
 * dialog.setOnConfirm(() -> {
 *     reservationService.createReservation(... );
 *     Notification.show("Reservation confirmed!");
 * });
 *
 * dialog.setOnCancel(() -> {
 *     Notification.show("Reservation cancelled");
 * });
 *
 * dialog.open();
 *
 * // With custom content
 * FestiventConfirmDialog deleteDialog = new FestiventConfirmDialog(
 *     "Delete Event",
 *     "This action cannot be undone.",
 *     "Delete"
 * );
 *
 * Div warningContent = new Div();
 * warningContent.setText("⚠️ All reservations will also be cancelled.");
 * warningContent.getStyle().set("color", "var(--festivent-error)");
 * deleteDialog.setContent(warningContent);
 *
 * deleteDialog.setOnConfirm(() -> eventService.deleteEvent(eventId));
 * deleteDialog.open();
 * </pre>
 *
 * Technical constraints:
 * - No navigation logic
 * - No service/repository calls
 * - Domain-agnostic (no business logic)
 * - Callbacks handled by caller
 */
public class FestiventConfirmDialog extends Dialog {

    private final H3 titleLabel;
    private final Span descriptionLabel;
    private final Div contentContainer;
    private final Button cancelButton;
    private final Button confirmButton;

    private Runnable onConfirmCallback;
    private Runnable onCancelCallback;

    /**
     * Creates a confirmation dialog with title, description, and confirm button text.
     *
     * @param title The dialog title (e.g., "Confirm Reservation")
     * @param description Optional description text (can be null)
     * @param confirmButtonText Text for the confirm button (e.g., "Confirm", "Delete", "Proceed")
     */
    public FestiventConfirmDialog(String title, String description, String confirmButtonText) {
        // Dialog configuration
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
        setWidth("500px");

        // Dialog styling
        getElement().getThemeList().add("festivent-dialog");

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle()
                .set("background-color", "var(--festivent-surface)")
                .set("border-radius", "var(--festivent-radius-xl)")
                .set("overflow", "hidden");

        // Header section
        HorizontalLayout header = createHeader(title);

        // Description section (optional)
        this.descriptionLabel = new Span(description);
        descriptionLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0 var(--festivent-space-xl) var(--festivent-space-md)")
                .set("display", (description != null && ! description.isBlank()) ? "block" : "none");

        // Content container (flexible area)
        this.contentContainer = new Div();
        contentContainer.getStyle()
                .set("padding", "0 var(--festivent-space-xl) var(--festivent-space-lg)")
                .set("flex", "1");

        // Footer with action buttons
        HorizontalLayout footer = createFooter(confirmButtonText);

        // Store references for API access
        this.titleLabel = (H3) header.getComponentAt(0);
        this.cancelButton = (Button) footer.getComponentAt(0);
        this.confirmButton = (Button) footer.getComponentAt(1);

        // Assemble dialog
        mainLayout.add(header, descriptionLabel, contentContainer, footer);
        add(mainLayout);
    }

    /**
     * Constructor without description (description can be set later).
     */
    public FestiventConfirmDialog(String title, String confirmButtonText) {
        this(title, null, confirmButtonText);
    }

    /**
     * Creates the header section with title and close button.
     */
    private HorizontalLayout createHeader(String title) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.getStyle()
                .set("padding", "var(--festivent-space-lg) var(--festivent-space-xl)")
                .set("border-bottom", "1px solid var(--festivent-border)");

        // Title
        H3 titleLabel = new H3(title);
        titleLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        // Close button (X)
        Button closeButton = new Button(VaadinIcon. CLOSE.create());
        closeButton.addThemeVariants(ButtonVariant. LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        closeButton.getStyle().set("color", "var(--lumo-secondary-text-color)");
        closeButton. addClickListener(e -> handleCancel());

        header.add(titleLabel, closeButton);
        return header;
    }

    /**
     * Creates the footer section with cancel and confirm buttons.
     */
    private HorizontalLayout createFooter(String confirmButtonText) {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setSpacing(true);
        footer.getStyle()
                .set("padding", "var(--festivent-space-lg) var(--festivent-space-xl)")
                .set("border-top", "1px solid var(--festivent-border)")
                .set("gap", "var(--festivent-space-sm)");

        // Cancel button (secondary style)
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> handleCancel());

        // Confirm button (primary style)
        Button confirmBtn = new Button(confirmButtonText);
        confirmBtn.addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        confirmBtn.addClickListener(e -> handleConfirm());

        footer.add(cancelBtn, confirmBtn);
        return footer;
    }

    /**
     * Handles the confirm action.
     */
    private void handleConfirm() {
        if (onConfirmCallback != null) {
            onConfirmCallback.run();
        }
        close();
    }

    /**
     * Handles the cancel action.
     */
    private void handleCancel() {
        if (onCancelCallback != null) {
            onCancelCallback. run();
        }
        close();
    }

    /**
     * Sets the callback to execute when confirm is clicked.
     *
     * @param callback The callback to execute
     */
    public void setOnConfirm(Runnable callback) {
        this.onConfirmCallback = callback;
    }

    /**
     * Sets the callback to execute when cancel is clicked.
     *
     * @param callback The callback to execute
     */
    public void setOnCancel(Runnable callback) {
        this.onCancelCallback = callback;
    }

    /**
     * Sets custom content in the dialog body.
     *
     * @param content The content component to display
     */
    public void setContent(Component content) {
        contentContainer.removeAll();
        if (content != null) {
            contentContainer.add(content);
        }
    }

    /**
     * Updates the dialog title.
     *
     * @param title The new title text
     */
    public void setTitle(String title) {
        titleLabel.setText(title != null ? title : "");
    }

    /**
     * Updates the description text.
     *
     * @param description The new description text
     */
    public void setDescription(String description) {
        boolean hasDescription = (description != null && !description.isBlank());
        descriptionLabel.setText(description != null ? description : "");
        descriptionLabel.getStyle().set("display", hasDescription ? "block" : "none");
    }

    /**
     * Updates the confirm button text.
     *
     * @param text The new button text
     */
    public void setConfirmButtonText(String text) {
        confirmButton.setText(text != null ? text : "Confirm");
    }

    /**
     * Updates the cancel button text.
     *
     * @param text The new button text
     */
    public void setCancelButtonText(String text) {
        cancelButton.setText(text != null ? text : "Cancel");
    }

    /**
     * Sets the confirm button style/theme variant.
     *
     * @param variant The button variant (e.g., ButtonVariant.LUMO_ERROR for destructive actions)
     */
    public void setConfirmButtonVariant(ButtonVariant variant) {
        confirmButton.getThemeNames().clear();
        if (variant != null) {
            confirmButton. addThemeVariants(variant);
        }
    }

    /**
     * Enables or disables the confirm button.
     *
     * @param enabled true to enable, false to disable
     */
    public void setConfirmEnabled(boolean enabled) {
        confirmButton.setEnabled(enabled);
    }

    /**
     * Gets the confirm button (for advanced customization).
     *
     * @return The confirm button
     */
    public Button getConfirmButton() {
        return confirmButton;
    }

    /**
     * Gets the cancel button (for advanced customization).
     *
     * @return The cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }
}