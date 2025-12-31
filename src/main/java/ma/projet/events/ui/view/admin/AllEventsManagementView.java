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
import com.vaadin.flow.component.html.*;
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
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;
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

        addClassNames(LumoUtility.Background.BASE);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        configureGrid();

        // 1. Header
        add(createHeader());

        // 2. Filtres
        add(createFilterLayout());

        // 3. Grid dans Wrapper
        VerticalLayout gridWrapper = new VerticalLayout(grid);
        gridWrapper.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Overflow.HIDDEN);
        gridWrapper.setPadding(false);
        gridWrapper.setSizeFull();
        add(gridWrapper);

        updateList();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Catalogue Global");
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XLARGE);

        Button refreshBtn = new Button("Actualiser", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> updateList());

        header.add(title, refreshBtn);
        return header;
    }

    private void configureGrid() {
        grid = new Grid<>(Event.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        grid.addColumn(Event::getTitre).setHeader("Titre").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(event -> event.getOrganisateur() != null ? event.getOrganisateur().getNomComplet() : "--").setHeader("Organisateur").setSortable(true).setAutoWidth(true);
        grid.addColumn(Event::getCategorie).setHeader("Catégorie").setSortable(true).setAutoWidth(true);
        grid.addColumn(event -> event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setHeader("Date").setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(event -> new StatusBadge(event.getStatut().getLabel(), event.getStatut().getColor()))).setHeader("Statut").setSortable(true).setKey("statut").setAutoWidth(true);
        grid.addComponentColumn(this::createActions).setHeader("Actions").setAutoWidth(true).setFlexGrow(0).setFrozenToEnd(true);
    }

    private Component createActions(Event event) {
        HorizontalLayout actions = new HorizontalLayout();

        Button viewBtn = new Button(new Icon(VaadinIcon.EYE));
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        viewBtn.setTooltipText("Détails");
        viewBtn.addClickListener(e -> showEventPreview(event));

        Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        editBtn.setTooltipText("Modifier (Admin)");
        editBtn.addClickListener(e -> navigationManager.goToEditEvent(event.getId()));
        editBtn.setEnabled(event.isModifiable());

        Button publishBtn = new Button(new Icon(VaadinIcon.CHECK));
        publishBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
        publishBtn.setTooltipText("Valider/Publier");
        publishBtn.setVisible(event.getStatut() == EventStatus.BROUILLON);
        publishBtn.addClickListener(e -> publishEvent(event));

        Button cancelBtn = new Button(new Icon(VaadinIcon.BAN));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        cancelBtn.setTooltipText("Annuler");
        cancelBtn.setVisible(event.getStatut() == EventStatus.PUBLIE);
        cancelBtn.addClickListener(e -> openCancelDialog(event));

        Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        deleteBtn.setTooltipText("Supprimer");
        deleteBtn.addClickListener(e -> deleteEvent(event));

        actions.add(viewBtn, editBtn, publishBtn, cancelBtn, deleteBtn);
        return actions;
    }

    private Component createFilterLayout() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshFilter());
        searchField.addClassName(LumoUtility.Flex.GROW);

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> refreshFilter());
        statusFilter.setWidth("180px");

        categoryFilter = new ComboBox<>();
        categoryFilter.setPlaceholder("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(EventCategory::getLabel);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> refreshFilter());
        categoryFilter.setWidth("180px");

        dateFilter = new DatePicker();
        dateFilter.setPlaceholder("Date min");
        dateFilter.setClearButtonVisible(true);
        dateFilter.addValueChangeListener(e -> refreshFilter());
        dateFilter.setWidth("180px");

        HorizontalLayout filters = new HorizontalLayout(searchField, statusFilter, categoryFilter, dateFilter);
        filters.setWidthFull();
        filters.addClassName(LumoUtility.Gap.MEDIUM);
        filters.setAlignItems(FlexComponent.Alignment.CENTER);
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
            String searchTerm = searchField.getValue();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String lowerTerm = searchTerm.toLowerCase();
                matchesSearch = event.getTitre().toLowerCase().contains(lowerTerm) ||
                        (event.getOrganisateur() != null && event.getOrganisateur().getNomComplet().toLowerCase().contains(lowerTerm));
            }
            boolean matchesStatus = statusFilter.getValue() == null || event.getStatut() == statusFilter.getValue();
            boolean matchesCategory = categoryFilter.getValue() == null || event.getCategorie() == categoryFilter.getValue();
            boolean matchesDate = dateFilter.getValue() == null || event.getDateDebut().toLocalDate().isAfter(dateFilter.getValue().minusDays(1));

            return matchesSearch && matchesStatus && matchesCategory && matchesDate;
        });
    }

    // --- MODALE APERÇU (Version propre) ---
    private void showEventPreview(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aperçu : " + event.getTitre());
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            Image image = new Image(event.getImageUrl(), "Cover");
            image.setWidthFull();
            image.setHeight("200px");
            image.getStyle().set("object-fit", "cover");
            image.addClassName(LumoUtility.BorderRadius.MEDIUM);
            content.add(image);
        }

        HorizontalLayout details = new HorizontalLayout(
                createDetailItem(VaadinIcon.CALENDAR, event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))),
                createDetailItem(VaadinIcon.MAP_MARKER, event.getVille())
        );
        details.setSpacing(true);

        StatusBadge badge = new StatusBadge(event.getStatut().getLabel(), event.getStatut().getColor());
        Paragraph desc = new Paragraph(event.getDescription());
        desc.addClassName(LumoUtility.TextColor.SECONDARY);

        content.add(badge, details, desc);

        Button closeBtn = new Button("Fermer", e -> dialog.close());
        dialog.getFooter().add(closeBtn);
        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createDetailItem(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("16px");
        i.addClassName(LumoUtility.TextColor.SECONDARY);
        return new HorizontalLayout(i, new Span(text));
    }

    // --- ACTIONS MÉTIER ---
    private Long getCurrentAdminId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getId).orElseThrow(() -> new IllegalStateException("Admin introuvable"));
    }

    private void publishEvent(Event event) {
        try {
            eventService.publishEvent(event.getId(), getCurrentAdminId());
            showNotification("Publié !", false);
            updateList();
        } catch (Exception e) { showNotification("Erreur : " + e.getMessage(), true); }
    }

    private void deleteEvent(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Supprimer ?");
        dialog.add(new Span("Irréversible pour : " + event.getTitre()));

        Button confirm = new Button("Supprimer", e -> {
            try {
                eventService.deleteEventSafely(event.getId(), getCurrentAdminId());
                showNotification("Supprimé.", false);
                updateList();
                dialog.close();
            } catch (Exception ex) { showNotification(ex.getMessage(), true); }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(new Button("Annuler", e -> dialog.close()), confirm);
        dialog.open();
    }

    private void openCancelDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Annuler l'événement");
        TextArea reason = new TextArea("Motif");
        reason.setWidthFull();
        dialog.add(reason);

        Button confirm = new Button("Confirmer", e -> {
            if(reason.isEmpty()) { reason.setInvalid(true); return; }
            try {
                eventService.cancelEvent(event.getId(), getCurrentAdminId(), reason.getValue());
                showNotification("Annulé.", false);
                updateList();
                dialog.close();
            } catch (Exception ex) { showNotification(ex.getMessage(), true); }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(new Button("Retour", e -> dialog.close()), confirm);
        dialog.open();
    }

    private void showNotification(String message, boolean isError) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(isError ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
    }
}