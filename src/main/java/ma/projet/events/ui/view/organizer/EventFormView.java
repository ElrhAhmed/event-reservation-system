package ma.projet.events.ui.view. organizer;

import com.vaadin. flow.component.UI;
import com. vaadin.flow. component.button.Button;
import com. vaadin.flow. component.button.ButtonVariant;
import com.vaadin. flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker. DateTimePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component. notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com. vaadin.flow. component.textfield. TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin. flow.router.*;
import jakarta.annotation.security. RolesAllowed;
import ma.projet.events. entity.Event;
import ma.projet.events.entity.EventCategory;
import ma. projet.events.entity.EventStatus;
import ma. projet.events.entity.User;
import ma. projet.events.exception.BusinessException;
import ma.projet.events. exception.ResourceNotFoundException;
import ma.projet. events.security.SecurityService;
import ma. projet.events.service.EventService;
import ma.projet. events.ui.layout.MainLayout;

import java.time.Duration;
import java. time.LocalDateTime;
import java.util. Arrays;
import java. util.List;

/**
 * Formulaire de création/modification d'événement
 */
@Route(value = "organizer/event/new", layout = MainLayout.class)
@RouteAlias(value = "organizer/event", layout = MainLayout.class)
@PageTitle("Create Event - EventReserve")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class EventFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final SecurityService securityService;
    private final EventService eventService;
    private final User currentUser;

    // Mode édition
    private boolean isEditMode = false;
    private Long eventId;
    private Event existingEvent;

    // Champs du formulaire
    private TextField titleField;
    private TextArea descriptionField;
    private ComboBox<EventCategory> categoryCombo;
    private ComboBox<String> cityCombo;
    private TextField locationField;
    private DateTimePicker startDateTimePicker;
    private DateTimePicker endDateTimePicker;
    private NumberField capacityField;
    private NumberField priceField;
    private TextField imageUrlField;

    // Preview
    private Image previewImage;
    private Span previewTitle;
    private Span previewInfoSpan;

    // Actions
    private Button publishButton;
    private Button saveDraftButton;

    // Liste des villes marocaines
    private static final List<String> CITIES = Arrays. asList(
            "Casablanca", "Rabat", "Marrakech", "Fès", "Tanger",
            "Agadir", "Meknès", "Oujda", "Kenitra", "Tétouan",
            "Salé", "Nador", "Mohammedia", "El Jadida", "Essaouira",
            "Paris", "Lyon", "Marseille"
    );

    private static final String DEFAULT_IMAGE = "https://images.unsplash. com/photo-1492684223066-81342ee5ff30?w=400";

    public EventFormView(SecurityService securityService, EventService eventService) {
        this. securityService = securityService;
        this.eventService = eventService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        if (parameter != null && ! parameter.equals("new")) {
            // Mode édition
            try {
                if (parameter.endsWith("/edit")) {
                    eventId = Long.parseLong(parameter.replace("/edit", ""));
                } else {
                    eventId = Long. parseLong(parameter);
                }
                existingEvent = eventService.getEventById(eventId);
                isEditMode = true;
            } catch (NumberFormatException | ResourceNotFoundException e) {
                Notification. show("Event not found", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                beforeEvent.rerouteTo("organizer/events");
                return;
            }
        }

        buildUI();
    }

    /**
     * Construit l'interface utilisateur
     */
    private void buildUI() {
        removeAll();

        // Back link + Header
        add(createBackLink());
        add(createHeader());

        // Main content:  2 colonnes
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);
        mainContent. getStyle().set("gap", "var(--festivent-space-xl)");

        // Colonne gauche: Formulaire
        VerticalLayout formColumn = new VerticalLayout();
        formColumn.setPadding(false);
        formColumn. setSpacing(true);
        formColumn.getStyle()
                .set("flex", "2")
                .set("gap", "var(--festivent-space-lg)");

        formColumn.add(
                createBasicInfoSection(),
                createDateTimeSection(),
                createCapacityPricingSection()
        );

        // Colonne droite: Preview + Actions
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn. getStyle()
                .set("flex", "1")
                .set("gap", "var(--festivent-space-lg)");

        rightColumn.add(
                createPreviewSection(),
                createActionsSection()
        );

        mainContent.add(formColumn, rightColumn);
        add(mainContent);

        // Remplir les champs si mode édition
        if (isEditMode && existingEvent != null) {
            populateFields();
        }
    }

    /**
     * Lien retour
     */
    private HorizontalLayout createBackLink() {
        HorizontalLayout backNav = new HorizontalLayout();
        backNav.setPadding(false);
        backNav. setSpacing(true);
        backNav.setAlignItems(FlexComponent.Alignment.CENTER);
        backNav.getStyle()
                .set("cursor", "pointer")
                .set("gap", "0.5rem");

        Icon backIcon = VaadinIcon. ARROW_LEFT. create();
        backIcon.setSize("18px");
        backIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span backLabel = new Span("Back");
        backLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        backNav.add(backIcon, backLabel);
        backNav.addClickListener(e -> UI.getCurrent().navigate("organizer/events"));

        // Hover effect
        backNav. getElement().addEventListener("mouseenter", ev -> {
            backIcon.getStyle().set("color", "var(--festivent-primary)");
            backLabel.getStyle().set("color", "var(--festivent-primary)");
        });
        backNav.getElement().addEventListener("mouseleave", ev -> {
            backIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");
            backLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        });

        return backNav;
    }

    /**
     * Header
     */
    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("margin-bottom", "var(--festivent-space-xl)");

        H2 title = new H2(isEditMode ? "Edit Event" : "Create New Event");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Fill in the details for your event");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        header.add(title, subtitle);
        return header;
    }

    /**
     * Section Basic Information
     */
    private Div createBasicInfoSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("gap", "var(--festivent-space-md)");

        H3 sectionTitle = new H3("Basic Information");
        sectionTitle.getStyle()
                .set("margin", "0 0 var(--festivent-space-md) 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Title
        titleField = new TextField("Title");
        titleField. setPlaceholder("Event title");
        titleField.setWidthFull();
        titleField.setRequired(true);
        titleField.addValueChangeListener(e -> updatePreview());

        // Description
        descriptionField = new TextArea("Description");
        descriptionField. setPlaceholder("Describe your event.. .");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("120px");
        descriptionField. setMaxLength(1000);

        // Category + City row
        HorizontalLayout categoryRow = new HorizontalLayout();
        categoryRow.setWidthFull();
        categoryRow. setSpacing(true);
        categoryRow.getStyle().set("gap", "var(--festivent-space-md)");

        categoryCombo = new ComboBox<>("Category");
        categoryCombo.setPlaceholder("Select category");
        categoryCombo.setItems(EventCategory.values());
        categoryCombo.setItemLabelGenerator(EventCategory::getLabel);
        categoryCombo.setWidthFull();
        categoryCombo.setRequired(true);

        cityCombo = new ComboBox<>("City");
        cityCombo.setPlaceholder("Select city");
        cityCombo.setItems(CITIES);
        cityCombo. setAllowCustomValue(true);
        cityCombo.addCustomValueSetListener(e -> cityCombo.setValue(e.getDetail()));
        cityCombo. setWidthFull();
        cityCombo. setRequired(true);
        cityCombo. addValueChangeListener(e -> updatePreview());

        categoryRow.add(categoryCombo, cityCombo);

        // Location
        locationField = new TextField("Location");
        locationField.setPlaceholder("Venue name or address");
        locationField.setWidthFull();
        locationField.setRequired(true);

        content.add(sectionTitle, titleField, descriptionField, categoryRow, locationField);
        card.add(content);
        return card;
    }

    /**
     * Section Date & Time
     */
    private Div createDateTimeSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 sectionTitle = new H3("Date & Time");
        sectionTitle.getStyle()
                .set("margin", "0 0 var(--festivent-space-md) 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        HorizontalLayout dateRow = new HorizontalLayout();
        dateRow.setWidthFull();
        dateRow.setSpacing(true);
        dateRow.getStyle().set("gap", "var(--festivent-space-md)");

        startDateTimePicker = new DateTimePicker("Start Date & Time");
        startDateTimePicker. setWidthFull();
        startDateTimePicker.setStep(Duration.ofMinutes(15));
        startDateTimePicker.setMin(LocalDateTime.now());

        endDateTimePicker = new DateTimePicker("End Date & Time");
        endDateTimePicker.setWidthFull();
        endDateTimePicker. setStep(Duration. ofMinutes(15));

        dateRow.add(startDateTimePicker, endDateTimePicker);

        content.add(sectionTitle, dateRow);
        card.add(content);
        return card;
    }

    /**
     * Section Capacity & Pricing + Image URL
     */
    private Div createCapacityPricingSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 sectionTitle = new H3("Capacity & Pricing");
        sectionTitle.getStyle()
                .set("margin", "0 0 var(--festivent-space-md) 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Capacity + Price row
        HorizontalLayout capacityRow = new HorizontalLayout();
        capacityRow.setWidthFull();
        capacityRow.setSpacing(true);
        capacityRow.getStyle().set("gap", "var(--festivent-space-md)");

        capacityField = new NumberField("Capacity");
        capacityField.setPlaceholder("Maximum attendees");
        capacityField.setWidthFull();
        capacityField. setMin(1);
        capacityField.setStep(1);

        priceField = new NumberField("Price (€)");
        priceField.setPlaceholder("0.00");
        priceField.setWidthFull();
        priceField.setMin(0);
        priceField.setStep(0.01);
        priceField. addValueChangeListener(e -> updatePreview());

        capacityRow.add(capacityField, priceField);

        // Image URL
        imageUrlField = new TextField("Image URL");
        imageUrlField. setPlaceholder("https://example.com/image. jpg");
        imageUrlField.setWidthFull();
        imageUrlField. setClearButtonVisible(true);
        imageUrlField. setHelperText("Enter a direct link to your event image (JPG, PNG, WebP)");
        imageUrlField. addValueChangeListener(e -> updatePreview());

        content.add(sectionTitle, capacityRow, imageUrlField);
        card.add(content);
        return card;
    }

    /**
     * Section Preview
     */
    private Div createPreviewSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 sectionTitle = new H3("Preview");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("How your event will appear");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-bottom", "var(--festivent-space-md)");

        // Preview card
        Div previewCard = new Div();
        previewCard. getStyle()
                .set("border", "1px solid var(--festivent-secondary)")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("overflow", "hidden");

        // Preview image placeholder
        Div previewImageContainer = new Div();
        previewImageContainer.getStyle()
                .set("width", "100%")
                .set("height", "120px")
                .set("background-color", "#e5e7eb")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden");

        previewImage = new Image(DEFAULT_IMAGE, "Event preview");
        previewImage.setWidthFull();
        previewImage. getStyle()
                .set("height", "120px")
                .set("object-fit", "cover");

        previewImageContainer.add(previewImage);

        // Preview info
        Div previewInfoDiv = new Div();
        previewInfoDiv.getStyle().set("padding", "var(--festivent-space-sm)");

        previewTitle = new Span("Event Title");
        previewTitle.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("color", "var(--festivent-primary)")
                .set("font-size", "var(--lumo-font-size-s)");

        previewInfoSpan = new Span("City • €0");
        previewInfoSpan.getStyle()
                .set("display", "block")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        previewInfoDiv.add(previewTitle, previewInfoSpan);
        previewCard.add(previewImageContainer, previewInfoDiv);

        content.add(sectionTitle, subtitle, previewCard);
        card.add(content);
        return card;
    }

    /**
     * Section Actions
     */
    private Div createActionsSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("gap", "var(--festivent-space-sm)");

        H3 sectionTitle = new H3("Actions");
        sectionTitle.getStyle()
                .set("margin", "0 0 var(--festivent-space-md) 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Publish button
        publishButton = new Button("Publish Event", VaadinIcon. PAPERPLANE. create());
        publishButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        publishButton.setWidthFull();
        publishButton.addClickListener(e -> saveEvent(true));

        // Save as draft button
        saveDraftButton = new Button("Save as Draft", VaadinIcon.ARCHIVE.create());
        saveDraftButton. addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        saveDraftButton.setWidthFull();
        saveDraftButton.getStyle().set("border", "1px solid var(--festivent-secondary)");
        saveDraftButton. addClickListener(e -> saveEvent(false));

        content.add(sectionTitle, publishButton, saveDraftButton);
        card.add(content);
        return card;
    }

    /**
     * Met à jour la preview
     */
    private void updatePreview() {
        // Title
        String title = titleField.getValue();
        previewTitle.setText(title. isEmpty() ? "Event Title" : title);

        // City + Price
        String city = cityCombo.getValue();
        Double price = priceField.getValue();
        String priceText = (price != null && price > 0) ? String.format("€%. 2f", price) : "Free";
        String cityText = (city != null && ! city.isEmpty()) ? city : "City";
        previewInfoSpan.setText(cityText + " • " + priceText);

        // Image
        String imageUrl = imageUrlField.getValue();
        if (imageUrl != null && !imageUrl.isBlank() && isValidImageUrl(imageUrl)) {
            previewImage.setSrc(imageUrl);
        } else if (isEditMode && existingEvent != null && existingEvent.getImageUrl() != null) {
            previewImage.setSrc(existingEvent. getImageUrl());
        } else {
            previewImage. setSrc(DEFAULT_IMAGE);
        }
    }

    /**
     * Vérifie si l'URL semble être une image valide
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String lowerUrl = url. toLowerCase();
        return lowerUrl.startsWith("http://") || lowerUrl. startsWith("https://");
    }

    /**
     * Remplit les champs en mode édition
     */
    private void populateFields() {
        titleField.setValue(existingEvent.getTitre() != null ? existingEvent.getTitre() : "");
        descriptionField. setValue(existingEvent.getDescription() != null ? existingEvent.getDescription() : "");
        categoryCombo.setValue(existingEvent.getCategorie());
        cityCombo.setValue(existingEvent.getVille());
        locationField.setValue(existingEvent.getLieu() != null ? existingEvent.getLieu() : "");
        startDateTimePicker. setValue(existingEvent.getDateDebut());
        endDateTimePicker.setValue(existingEvent.getDateFin());
        capacityField.setValue(existingEvent.getCapaciteMax() != null ? existingEvent.getCapaciteMax().doubleValue() : null);
        priceField.setValue(existingEvent.getPrixUnitaire());
        imageUrlField.setValue(existingEvent.getImageUrl() != null ? existingEvent.getImageUrl() : "");

        updatePreview();
    }

    /**
     * Sauvegarde l'événement
     */
    private void saveEvent(boolean publish) {
        // Validation
        if (! validateForm()) {
            return;
        }

        try {
            Event event;

            if (isEditMode) {
                event = existingEvent;
            } else {
                event = new Event();
            }

            // Remplir les données
            event.setTitre(titleField.getValue().trim());
            event.setDescription(descriptionField.getValue().trim());
            event.setCategorie(categoryCombo.getValue());
            event. setVille(cityCombo.getValue());
            event.setLieu(locationField.getValue().trim());
            event.setDateDebut(startDateTimePicker.getValue());
            event.setDateFin(endDateTimePicker.getValue());
            event. setCapaciteMax(capacityField. getValue().intValue());
            event.setPrixUnitaire(priceField.getValue() != null ? priceField.getValue() : 0.0);

            String imageUrl = imageUrlField.getValue();
            if (imageUrl != null && !imageUrl.isBlank()) {
                event. setImageUrl(imageUrl. trim());
            }

            if (isEditMode) {
                // Update
                eventService.updateEvent(eventId, event, currentUser.getId());

                if (publish && event.getStatut() == EventStatus.BROUILLON) {
                    eventService.publishEvent(eventId, currentUser.getId());
                }

                Notification.show("Event updated successfully", 3000, Notification.Position. TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
            } else {
                // Create
                Event savedEvent = eventService. createEvent(event, currentUser.getId());

                if (publish) {
                    eventService.publishEvent(savedEvent.getId(), currentUser.getId());
                    Notification.show("Event published successfully", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
                } else {
                    Notification.show("Event saved as draft", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
                }
            }

            UI.getCurrent().navigate("organizer/events");

        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 4000, Notification. Position.TOP_CENTER)
                    . addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (titleField.isEmpty()) {
            errors. append("Title is required.\n");
        }
        if (categoryCombo.isEmpty()) {
            errors. append("Category is required.\n");
        }
        if (cityCombo.isEmpty()) {
            errors. append("City is required.\n");
        }
        if (locationField.isEmpty()) {
            errors. append("Location is required.\n");
        }
        if (startDateTimePicker.isEmpty()) {
            errors.append("Start date & time is required.\n");
        }
        if (endDateTimePicker.isEmpty()) {
            errors.append("End date & time is required.\n");
        }
        if (capacityField.isEmpty() || capacityField.getValue() < 1) {
            errors.append("Capacity must be at least 1.\n");
        }

        if (startDateTimePicker. getValue() != null && endDateTimePicker.getValue() != null) {
            if (endDateTimePicker. getValue().isBefore(startDateTimePicker.getValue()) ||
                    endDateTimePicker.getValue().isEqual(startDateTimePicker.getValue())) {
                errors. append("End date must be after start date.\n");
            }
        }

        if (errors.length() > 0) {
            Notification.show(errors.toString(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        return true;
    }
}