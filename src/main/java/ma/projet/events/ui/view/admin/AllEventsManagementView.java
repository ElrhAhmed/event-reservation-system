package ma.projet.events.ui.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image; // Import ajouté
import com.vaadin.flow.component.html.Paragraph; // Import ajouté
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.User;
import ma.projet.events.repository.UserRepository;
import ma.projet.events.service.EventService;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat; // Import ajouté pour le formatage monétaire
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale; // Import ajouté pour le formatage monétaire
import java.util.Optional;

@Route(value = "admin/events", layout = AdminLayout.class)
@PageTitle("Gestion des Événements | FESTIVENT")
@RolesAllowed("ADMIN")
public class AllEventsManagementView extends VerticalLayout {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final UserRepository userRepository;

    private Grid<Event> grid;
    private GridListDataView<Event> dataView;

    // Filtres
    private TextField searchField;
    private ComboBox<EventStatus> statusFilter;
    private ComboBox<EventCategory> categoryFilter;
    private DatePicker dateFilter;

    public AllEventsManagementView(EventService eventService,
                                   NavigationManager navigationManager,
                                   UserRepository userRepository) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;
        this.userRepository = userRepository;

        addClassNames("all-events-view", LumoUtility.Padding.LARGE);
        setHeightFull();

        configureGrid();
        configureFilters();

        add(createHeader(), createFilterLayout(), grid);

        updateList();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Gestion Globale des Événements");
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.LARGE);

        Button refreshBtn = new Button("Actualiser", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addClickListener(e -> updateList());

        header.add(title, refreshBtn);
        return header;
    }

    private void configureGrid() {
        grid = new Grid<>(Event.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setSizeFull();

        // 1. Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        // 2. Organisateur
        grid.addColumn(event -> event.getOrganisateur() != null ?
                        event.getOrganisateur().getNomComplet() : "Inconnu")
                .setHeader("Organisateur")
                .setSortable(true)
                .setAutoWidth(true);

        // 3. Catégorie
        grid.addColumn(Event::getCategorie)
                .setHeader("Catégorie")
                .setSortable(true)
                .setAutoWidth(true);

        // 4. Date
        grid.addColumn(event -> event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date Début")
                .setSortable(true)
                .setAutoWidth(true);

        // 5. Statut (Badge)
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setSortable(true)
                .setKey("statut")
                .setAutoWidth(true);

        // 6. Actions
        grid.addColumn(new ComponentRenderer<>(this::createActions))
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    // Réutilisé pour la Grid ET pour la modale
    private Span createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().name()); // Utilise name() car on n'a pas getLabel() de l'enum
        String theme = switch (event.getStatut()) {
            case PUBLIE -> "badge success";
            case BROUILLON -> "badge contrast";
            case ANNULE -> "badge error";
            case TERMINE -> "badge";
        };
        badge.getElement().getThemeList().add(theme);
        return badge;
    }

    private Component createActions(Event event) {
        HorizontalLayout actions = new HorizontalLayout();

        // Voir (MODIFIÉ : Appelle showEventPreview au lieu de navigationManager.goToEventDetail)
        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewBtn.setTooltipText("Voir les détails");
        viewBtn.addClickListener(e -> showEventPreview(event));

        // Modifier
        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editBtn.setTooltipText("Modifier (Admin)");
        editBtn.addClickListener(e -> navigationManager.goToEditEvent(event.getId()));
        editBtn.setEnabled(event.isModifiable());

        // Publier (visible seulement si BROUILLON)
        Button publishBtn = new Button(new Icon(VaadinIcon.CHECK));
        publishBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        publishBtn.setTooltipText("Publier");
        publishBtn.setVisible(event.getStatut() == EventStatus.BROUILLON);
        publishBtn.addClickListener(e -> publishEvent(event));

        // Annuler (visible seulement si PUBLIE)
        Button cancelBtn = new Button(new Icon(VaadinIcon.BAN));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setTooltipText("Annuler");
        cancelBtn.setVisible(event.getStatut() == EventStatus.PUBLIE);
        cancelBtn.addClickListener(e -> openCancelDialog(event));

        // Supprimer
        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.setTooltipText("Supprimer définitivement");
        deleteBtn.addClickListener(e -> deleteEvent(event));

        actions.add(viewBtn, editBtn, publishBtn, cancelBtn, deleteBtn);
        return actions;
    }

    private void configureFilters() {
        searchField = new TextField();
        searchField.setPlaceholder("Recherche (Titre, Lieu, Orga)...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshFilter());

        statusFilter = new ComboBox<>("Statut", EventStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> refreshFilter());

        categoryFilter = new ComboBox<>("Catégorie", EventCategory.values());
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> refreshFilter());

        dateFilter = new DatePicker("Date min.");
        dateFilter.setClearButtonVisible(true);
        dateFilter.addValueChangeListener(e -> refreshFilter());
    }

    private Component createFilterLayout() {
        HorizontalLayout filters = new HorizontalLayout(searchField, statusFilter, categoryFilter, dateFilter);
        filters.setWidthFull();
        filters.addClassName(LumoUtility.Gap.MEDIUM);
        filters.setAlignItems(FlexComponent.Alignment.END);
        searchField.addClassName(LumoUtility.Flex.GROW);
        return filters;
    }

    private void updateList() {
        List<Event> allEvents = eventService.getAllEvents();
        dataView = grid.setItems(allEvents);
        refreshFilter();
    }

    private void refreshFilter() {
        if (dataView == null) return;

        dataView.setFilter(event -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;
            boolean matchesCategory = true;
            boolean matchesDate = true;

            String searchTerm = searchField.getValue();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String lowerTerm = searchTerm.toLowerCase();
                boolean inTitle = event.getTitre().toLowerCase().contains(lowerTerm);
                boolean inLoc = event.getLieu() != null && event.getLieu().toLowerCase().contains(lowerTerm);
                boolean inCity = event.getVille() != null && event.getVille().toLowerCase().contains(lowerTerm);
                boolean inOrg = event.getOrganisateur() != null &&
                        event.getOrganisateur().getNomComplet().toLowerCase().contains(lowerTerm);

                matchesSearch = inTitle || inLoc || inCity || inOrg;
            }

            if (statusFilter.getValue() != null) {
                matchesStatus = event.getStatut() == statusFilter.getValue();
            }

            if (categoryFilter.getValue() != null) {
                matchesCategory = event.getCategorie() == categoryFilter.getValue();
            }

            if (dateFilter.getValue() != null) {
                matchesDate = event.getDateDebut().toLocalDate().isAfter(dateFilter.getValue().minusDays(1));
            }

            return matchesSearch && matchesStatus && matchesCategory && matchesDate;
        });
    }

    // ==================== MÉTHODES POUR LA MODALE D'APERÇU ====================
    private void showEventPreview(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aperçu : " + event.getTitre());
        dialog.setWidth("600px");
        dialog.setMaxWidth("90vw");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);
        content.addClassName(LumoUtility.Padding.MEDIUM);

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
        detailsRow.addClassName(LumoUtility.Gap.MEDIUM);

        // Formatage de la date
        String formattedDate = event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Formatage du prix (en DH, comme exemple)
        // Utilisation de NumberFormat pour une meilleure internationalisation future
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "MA")); // Marocain français
        String formattedPrice = currencyFormat.format(event.getPrixUnitaire());


        VerticalLayout leftInfo = new VerticalLayout(
                createDetailItem(VaadinIcon.CALENDAR, formattedDate),
                createDetailItem(VaadinIcon.MAP_MARKER, event.getVille() + " - " + event.getLieu())
        );

        VerticalLayout rightInfo = new VerticalLayout(
                createDetailItem(VaadinIcon.MONEY, formattedPrice),
                createDetailItem(VaadinIcon.GROUP, event.getCapaciteMax() + " places max")
        );

        leftInfo.setPadding(false);
        rightInfo.setPadding(false);
        leftInfo.setSpacing(false); // Pas d'espace excessif
        rightInfo.setSpacing(false); // Pas d'espace excessif
        detailsRow.add(leftInfo, rightInfo);
        detailsRow.setFlexGrow(1, leftInfo, rightInfo);

        // 3. Status Badge (Réutilisation de la méthode existante)
        Span statusBadge = createStatusBadge(event);
        statusBadge.addClassName(LumoUtility.Margin.Top.SMALL);

        // 4. Description
        Span descLabel = new Span("Description");
        descLabel.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.Margin.Top.MEDIUM);
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
        HorizontalLayout layout = new HorizontalLayout(i, s);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);
        return layout;
    }
    // ==================== FIN MODALE D'APERÇU ====================


    // ==================== ACTIONS MÉTIER ====================
    private Long getCurrentAdminId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getId).orElseThrow(() -> new IllegalStateException("Admin connecté introuvable (Email: " + email + ")"));
    }

    private void publishEvent(Event event) {
        try {
            eventService.publishEvent(event.getId(), getCurrentAdminId());
            showNotification("Événement publié avec succès", false);
            updateList();
        } catch (Exception e) {
            showNotification("Erreur : " + e.getMessage(), true);
        }
    }

    private void deleteEvent(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Supprimer l'événement ?");

        Div content = new Div(new Span("Êtes-vous sûr de vouloir supprimer définitivement \"" + event.getTitre() + "\" ?"));
        dialog.add(content);

        Button confirmBtn = new Button("Supprimer", e -> {
            try {
                eventService.deleteEventSafely(event.getId(), getCurrentAdminId());
                showNotification("Événement supprimé", false);
                updateList();
                dialog.close();
            } catch (Exception ex) {
                showNotification("Impossible de supprimer : " + ex.getMessage(), true);
                dialog.close();
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Annuler", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }

    private void openCancelDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Annuler l'événement");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextArea reasonField = new TextArea("Motif de l'annulation (obligatoire)");
        reasonField.setWidthFull();
        dialogLayout.add(new Span("Ceci annulera toutes les réservations associées."), reasonField);
        dialog.add(dialogLayout);

        Button confirmBtn = new Button("Confirmer l'annulation", e -> {
            if (reasonField.getValue().isBlank()) {
                reasonField.setInvalid(true);
                reasonField.setErrorMessage("Le motif est requis");
                return;
            }
            try {
                eventService.cancelEvent(event.getId(), getCurrentAdminId(), reasonField.getValue());
                showNotification("Événement annulé", false);
                updateList();
                dialog.close();
            }         catch (Exception ex) {
                showNotification("Erreur : " + ex.getMessage(), true);
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Retour", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }

    private void showNotification(String message, boolean isError) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(isError ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.BOTTOM_END);
        notification.setDuration(3000);
    }
}