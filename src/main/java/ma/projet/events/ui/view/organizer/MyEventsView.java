package ma.projet.events.ui.view. organizer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin. flow.component.grid.Grid;
import com.vaadin. flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component. notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com. vaadin.flow. component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com. vaadin.flow. data.value.ValueChangeMode;
import com. vaadin.flow. router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security. RolesAllowed;
import ma.projet.events. entity.Event;
import ma.projet.events.entity.EventStatus;
import ma. projet.events.entity.User;
import ma. projet.events.exception.BusinessException;
import ma.projet.events. exception.UnauthorizedException;
import ma.projet.events.security.SecurityService;
import ma. projet.events.service.EventService;
import ma.projet. events.ui.layout.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util. List;
import java.util. Locale;
import java.util. stream.Collectors;

/**
 * Vue "Mes Événements" pour les organisateurs
 */
@Route(value = "organizer/events", layout = MainLayout.class)
@PageTitle("My Events - EventReserve")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final User currentUser;

    // Composants UI
    private TextField searchField;
    private ComboBox<EventStatus> statusFilter;
    private Grid<Event> grid;
    private Span countLabel;

    // Données
    private List<Event> allEvents;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    public MyEventsView(SecurityService securityService, EventService eventService) {
        this.eventService = eventService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)");

        add(
                createHeaderSection(),
                createFiltersSection(),
                createGridSection()
        );

        // Charger les données
        loadEvents();
    }

    /**
     * Section header avec titre et bouton Create Event
     */
    private HorizontalLayout createHeaderSection() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        header.setAlignItems(FlexComponent.Alignment. CENTER);
        header.getStyle().set("margin-bottom", "var(--festivent-space-lg)");

        // Titre
        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection. setSpacing(false);

        H2 title = new H2("My Events");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Manage your events and view reservations");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        titleSection.add(title, subtitle);

        // Bouton Create Event
        Button createEventButton = new Button("Create Event", VaadinIcon. PLUS. create());
        createEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createEventButton. addClickListener(e -> UI.getCurrent().navigate("organizer/event/new"));

        header.add(titleSection, createEventButton);
        return header;
    }

    /**
     * Section des filtres
     */
    private HorizontalLayout createFiltersSection() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment. END);
        filters.setSpacing(true);
        filters.getStyle()
                .set("gap", "var(--festivent-space-md)")
                .set("margin-bottom", "var(--festivent-space-lg)");

        // Champ de recherche
        searchField = new TextField();
        searchField. setPlaceholder("Search events...");
        searchField.setPrefixComponent(VaadinIcon. SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        // Filtre par statut
        statusFilter = new ComboBox<>();
        statusFilter. setPlaceholder("All Status");
        statusFilter.setItems(EventStatus. values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter. setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());
        statusFilter.setWidth("180px");

        filters.add(searchField, statusFilter);
        filters.setFlexGrow(1, searchField);

        return filters;
    }

    /**
     * Section de la grille
     */
    private Div createGridSection() {
        Div section = new Div();
        section.addClassName("festivent-card");
        section.setSizeFull();
        section.getStyle().set("padding", "var(--festivent-space-lg)");

        // Header avec compteur
        countLabel = new Span("Events (0)");
        countLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin-bottom", "var(--festivent-space-md)")
                .set("display", "block");

        // Création de la grille
        grid = new Grid<>(Event.class, false);
        grid.setWidthFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant. LUMO_ROW_STRIPES);

        // Colonnes
        grid. addColumn(Event:: getTitre)
                .setHeader("Title")
                .setFlexGrow(2)
                .setSortable(true);

        grid.addColumn(event -> event.getCategorie().getLabel())
                .setHeader("Category")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addColumn(event -> event.getDateDebut().format(DATE_FORMATTER))
                .setHeader("Date")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addComponentColumn(this::createSeatsColumn)
                .setHeader("Seats")
                .setFlexGrow(0)
                .setWidth("100px");

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setFlexGrow(0)
                .setWidth("120px");

        grid.addComponentColumn(this::createActionsColumn)
                .setHeader("Actions")
                .setFlexGrow(0)
                .setWidth("160px");

        section.add(countLabel, grid);
        return section;
    }

    /**
     * Crée la colonne des places
     */
    private Span createSeatsColumn(Event event) {
        int availableSeats = eventService.calculateAvailablePlaces(event. getId());
        int totalSeats = event.getCapaciteMax();
        int bookedSeats = totalSeats - availableSeats;

        Span seats = new Span(bookedSeats + "/" + totalSeats);
        seats.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "500");

        return seats;
    }

    /**
     * Crée le badge de statut
     */
    private Span createStatusBadge(Event event) {
        EventStatus status = event.getStatut();
        Span badge = new Span(status.getLabel());

        String backgroundColor;
        String textColor;

        switch (status) {
            case PUBLIE -> {
                backgroundColor = "#dcfce7";
                textColor = "#166534";
            }
            case BROUILLON -> {
                backgroundColor = "#fef3c7";
                textColor = "#92400e";
            }
            case ANNULE -> {
                backgroundColor = "#fee2e2";
                textColor = "#991b1b";
            }
            case TERMINE -> {
                backgroundColor = "#f3f4f6";
                textColor = "#6b7280";
            }
            default -> {
                backgroundColor = "#f3f4f6";
                textColor = "#6b7280";
            }
        }

        badge.getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "9999px")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "500")
                .set("background-color", backgroundColor)
                .set("color", textColor);

        return badge;
    }

    /**
     * Crée la colonne des actions
     */
    private HorizontalLayout createActionsColumn(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("gap", "var(--festivent-space-xs)");

        // Bouton voir (réservations)
        Button viewButton = new Button(VaadinIcon.EYE.create());
        viewButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY, ButtonVariant. LUMO_ICON);
        viewButton.getStyle().set("color", "var(--lumo-secondary-text-color)");
        viewButton.getElement().setAttribute("title", "View reservations");
        viewButton.addClickListener(e ->
                UI.getCurrent().navigate("organizer/event-reservations/" + event.getId()));

        // Bouton éditer
        Button editButton = new Button(VaadinIcon.EDIT. create());
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant. LUMO_ICON);
        editButton.getStyle().set("color", "var(--lumo-secondary-text-color)");
        editButton.getElement().setAttribute("title", "Edit event");
        editButton.setEnabled(event.isModifiable());
        if (! event.isModifiable()) {
            editButton.getStyle().set("opacity", "0.3");
        }
        editButton.addClickListener(e ->
                UI. getCurrent().navigate("organizer/event/" + event.getId() + "/edit"));

        // Bouton annuler
        Button cancelButton = new Button(VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant. LUMO_ICON);
        cancelButton.getStyle().set("color", "var(--lumo-secondary-text-color)");
        cancelButton.getElement().setAttribute("title", "Cancel event");
        boolean canCancel = event. getStatut() == EventStatus.PUBLIE ||
                event. getStatut() == EventStatus.BROUILLON;
        cancelButton. setEnabled(canCancel);
        if (! canCancel) {
            cancelButton. getStyle().set("opacity", "0.3");
        }
        cancelButton. addClickListener(e -> showCancelConfirmation(event));

        // Bouton supprimer
        Button deleteButton = new Button(VaadinIcon. TRASH.create());
        deleteButton. addThemeVariants(ButtonVariant. LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        deleteButton.getStyle().set("color", "var(--lumo-secondary-text-color)");
        deleteButton. getElement().setAttribute("title", "Delete event");
        deleteButton.addClickListener(e -> showDeleteConfirmation(event));

        actions.add(viewButton, editButton, cancelButton, deleteButton);
        return actions;
    }

    /**
     * Charge les événements
     */
    private void loadEvents() {
        allEvents = eventService. getEventsByOrganisateur(currentUser. getId());
        applyFilters();
    }

    /**
     * Applique les filtres
     */
    private void applyFilters() {
        String searchTerm = searchField.getValue().toLowerCase().trim();
        EventStatus status = statusFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> {
                    if (searchTerm. isEmpty()) return true;
                    return e.getTitre().toLowerCase().contains(searchTerm) ||
                            e.getDescription().toLowerCase().contains(searchTerm) ||
                            e. getVille().toLowerCase().contains(searchTerm);
                })
                .filter(e -> status == null || e.getStatut() == status)
                .collect(Collectors.toList());

        grid.setItems(filtered);
        countLabel.setText("Events (" + filtered.size() + ")");
    }

    /**
     * Dialog de confirmation d'annulation
     */
    private void showCancelConfirmation(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cancel Event");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span message = new Span("Are you sure you want to cancel this event?");
        message.getStyle().set("color", "var(--festivent-secondary-text)");

        Span eventName = new Span(event.getTitre());
        eventName.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span warning = new Span("All reservations will be cancelled and attendees will be notified.");
        warning.getStyle()
                .set("color", "#dc2626")
                .set("font-size", "var(--lumo-font-size-s)");

        content.add(message, eventName, warning);
        dialog.add(content);

        Button cancelButton = new Button("Keep Event", e -> dialog.close());
        cancelButton. addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmButton = new Button("Cancel Event", e -> {
            try {
                eventService.cancelEvent(event.getId(), currentUser.getId(), "Cancelled by organizer");
                Notification. show("Event cancelled successfully", 3000, Notification.Position. TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
                dialog. close();
                loadEvents();
            } catch (BusinessException | UnauthorizedException ex) {
                Notification.show(ex.getMessage(), 4000, Notification. Position.TOP_CENTER)
                        . addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant. LUMO_ERROR);

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    /**
     * Dialog de confirmation de suppression
     */
    private void showDeleteConfirmation(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete Event");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span message = new Span("Are you sure you want to delete this event?");
        message.getStyle().set("color", "var(--festivent-secondary-text)");

        Span eventName = new Span(event.getTitre());
        eventName.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span warning = new Span("This action cannot be undone.  Events with reservations cannot be deleted.");
        warning.getStyle()
                .set("color", "#dc2626")
                .set("font-size", "var(--lumo-font-size-s)");

        content.add(message, eventName, warning);
        dialog.add(content);

        Button cancelButton = new Button("Keep Event", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);

        Button confirmButton = new Button("Delete Event", e -> {
            try {
                eventService.deleteEventSafely(event.getId(), currentUser.getId());
                Notification. show("Event deleted successfully", 3000, Notification.Position. TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_SUCCESS);
                dialog.close();
                loadEvents();
            } catch (BusinessException | UnauthorizedException ex) {
                Notification.show(ex. getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }
}