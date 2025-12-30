package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.OrganizerLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "organizer/events", layout = OrganizerLayout.class)
@PageTitle("Mes Événements | FESTIVENT")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    private Grid<Event> grid;
    private TextField searchField;
    private ComboBox<EventStatus> statusFilter;

    private List<Event> allEvents;

    public MyEventsView(EventService eventService,
                        UserService userService,
                        SecurityService securityService,
                        NavigationManager navigationManager) {
        this.eventService = eventService;
        this.userService = userService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        setPadding(true);
        setSpacing(true);
        setSizeFull();
        addClassName(LumoUtility.Background.BASE);

        add(createHeader(), createToolbar());
        configureGrid();
        add(grid);
        refreshGrid();
    }

    // ... (createHeader et createToolbar restent inchangés) ...

    private HorizontalLayout createHeader() {
        H2 title = new H2("Gestion des Événements");
        title.addClassName(LumoUtility.Margin.NONE);

        Button createBtn = new Button("Créer un événement", new Icon(VaadinIcon.PLUS));
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(e -> navigationManager.goToCreateEvent());

        HorizontalLayout header = new HorizontalLayout(title, createBtn);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }

    private HorizontalLayout createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par titre...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Filtrer par statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void configureGrid() {
        grid = new Grid<>(Event.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.addColumn(Event::getTitre).setHeader("Titre").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(e -> e.getCategorie().getLabel()).setHeader("Catégorie").setSortable(true).setAutoWidth(true);
        grid.addColumn(e -> DateFormatter.format(e.getDateDebut())).setHeader("Date").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(e -> new StatusBadge(e.getStatut().getLabel(), e.getStatut().getColor()))).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createProgressComponent)).setHeader("Remplissage").setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setAutoWidth(true).setFrozenToEnd(true);
    }

    private Component createProgressComponent(Event event) {
        int available = eventService.calculateAvailablePlaces(event.getId());
        int total = event.getCapaciteMax();
        int reserved = total - available;
        double ratio = (double) reserved / total;
        if (ratio > 1.0) ratio = 1.0;

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(ratio);
        if (ratio >= 1.0) progressBar.addThemeVariants(com.vaadin.flow.component.progressbar.ProgressBarVariant.LUMO_ERROR);
        else if (ratio <= 0.8) progressBar.addThemeVariants(com.vaadin.flow.component.progressbar.ProgressBarVariant.LUMO_SUCCESS);

        progressBar.setHeight("8px");
        Span text = new Span(reserved + " / " + total);
        text.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout layout = new VerticalLayout(progressBar, text);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.END);
        return layout;
    }

    // --- MODIFICATION ICI : ACTIONS ---
    private Component createActionButtons(Event event) {
        HorizontalLayout actions = new HorizontalLayout();

        // 1. BOUTON APERÇU (Modale)
        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        viewBtn.setTooltipText("Aperçu rapide");
        viewBtn.addClickListener(e -> showEventPreview(event));

        // 2. ✅ NOUVEAU BOUTON : PARTICIPANTS
        Button participantsBtn = new Button(new Icon(VaadinIcon.GROUP)); // Icône "Groupe"
        participantsBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        participantsBtn.getStyle().set("color", "var(--lumo-primary-text-color)"); // Couleur bleue pour le distinguer
        participantsBtn.setTooltipText("Gérer les participants / réservations");
        participantsBtn.addClickListener(e -> navigationManager.goToEventReservations(event.getId()));

        // 3. BOUTON EDITER
        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        editBtn.setTooltipText("Modifier");
        editBtn.setVisible(event.isModifiable());
        editBtn.addClickListener(e -> navigationManager.goToEditEvent(event.getId()));

        // 4. BOUTON PUBLIER (Si Brouillon)
        Button publishBtn = new Button(new Icon(VaadinIcon.UPLOAD));
        publishBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS);
        publishBtn.setTooltipText("Publier");
        publishBtn.setVisible(event.getStatut() == EventStatus.BROUILLON);
        publishBtn.addClickListener(e -> handlePublish(event));

        // 5. BOUTON ANNULER (Si Publié)
        Button cancelBtn = new Button(new Icon(VaadinIcon.BAN));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        cancelBtn.setTooltipText("Annuler l'événement");
        cancelBtn.setVisible(event.getStatut() == EventStatus.PUBLIE);
        cancelBtn.addClickListener(e -> handleCancel(event));

        // 6. BOUTON SUPPRIMER (Si possible)
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        boolean canDelete = event.getStatut() == EventStatus.BROUILLON;
        deleteBtn.setVisible(canDelete);
        deleteBtn.addClickListener(e -> handleDelete(event));

        // Ajout de tous les boutons
        actions.add(viewBtn, participantsBtn, editBtn, publishBtn, cancelBtn, deleteBtn);
        return actions;
    }

    // --- NOUVELLE MÉTHODE : MODALE APERÇU ---
    private void showEventPreview(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aperçu : " + event.getTitre());
        dialog.setWidth("600px");
        dialog.setMaxWidth("90vw");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        // 1. Image
        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            Image image = new Image(event.getImageUrl(), "Cover");
            image.setWidthFull();
            image.setHeight("200px");
            image.getStyle().set("object-fit", "cover");
            image.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            content.add(image);
        }

        // 2. Infos Clés (Grille 2 colonnes)
        HorizontalLayout detailsRow = new HorizontalLayout();
        detailsRow.setWidthFull();

        VerticalLayout leftInfo = new VerticalLayout(
                createDetailItem(VaadinIcon.CALENDAR, DateFormatter.format(event.getDateDebut())),
                createDetailItem(VaadinIcon.MAP_MARKER, event.getVille() + " - " + event.getLieu())
        );

        VerticalLayout rightInfo = new VerticalLayout(
                createDetailItem(VaadinIcon.MONEY, PriceFormatter.format(event.getPrixUnitaire())),
                createDetailItem(VaadinIcon.GROUP, event.getCapaciteMax() + " places max")
        );

        leftInfo.setPadding(false);
        rightInfo.setPadding(false);
        detailsRow.add(leftInfo, rightInfo);

        // 3. Status Badge
        StatusBadge statusBadge = new StatusBadge(event.getStatut().getLabel(), event.getStatut().getColor());

        // 4. Description courte
        Span descLabel = new Span("Description");
        descLabel.addClassName(LumoUtility.FontWeight.BOLD);
        Paragraph desc = new Paragraph(event.getDescription());
        desc.getStyle().set("max-height", "150px");
        desc.getStyle().set("overflow-y", "auto");

        content.add(statusBadge, detailsRow, descLabel, desc);

        // Footer avec bouton Fermer
        Button closeBtn = new Button("Fermer", e -> dialog.close());
        dialog.getFooter().add(closeBtn);

        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createDetailItem(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("18px");
        i.addClassName(LumoUtility.TextColor.SECONDARY);
        Span s = new Span(text);
        s.addClassName(LumoUtility.FontSize.SMALL);
        return new HorizontalLayout(i, s);
    }

    // ... (Le reste des méthodes getCurrentOrganizer, refreshGrid, updateList, handlers... reste inchangé) ...
    // Assurez-vous d'avoir bien copié les méthodes handlePublish, handleCancel, handleDelete, showSuccess, showError et les utilitaires en bas de fichier.

    private User getCurrentOrganizer() {
        var userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) return userService.getUserByEmail(userDetails.getUsername());
        return null;
    }

    private void refreshGrid() {
        User organizer = getCurrentOrganizer();
        if (organizer != null) {
            allEvents = eventService.getEventsByOrganisateur(organizer.getId());
            allEvents.sort(Comparator.comparing(Event::getId).reversed());
            updateList();
        }
    }

    private void updateList() {
        if (allEvents == null) return;
        List<Event> filtered = allEvents.stream().filter(e -> {
            String search = searchField.getValue().trim().toLowerCase();
            boolean matchesSearch = search.isEmpty() || e.getTitre().toLowerCase().contains(search);
            boolean matchesStatus = statusFilter.getValue() == null || e.getStatut() == statusFilter.getValue();
            return matchesSearch && matchesStatus;
        }).collect(Collectors.toList());
        grid.setItems(filtered);
    }

    private void handlePublish(Event event) {
        ConfirmDialogUtil.show("Publier ?", "Visible par tous les clients.", () -> {
            try {
                eventService.publishEvent(event.getId(), getCurrentOrganizer().getId());
                showSuccess("Publié !");
                refreshGrid();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private void handleCancel(Event event) {
        ConfirmDialogUtil.show("Annuler ?", "Action irréversible.", () -> {
            try {
                eventService.cancelEvent(event.getId(), getCurrentOrganizer().getId(), "Annulé par org");
                showSuccess("Annulé.");
                refreshGrid();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private void handleDelete(Event event) {
        ConfirmDialogUtil.show("Supprimer ?", "Vraiment supprimer ?", () -> {
            try {
                eventService.deleteEventSafely(event.getId(), getCurrentOrganizer().getId());
                showSuccess("Supprimé.");
                refreshGrid();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}