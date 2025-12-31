package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.Reservation;
import ma.projet.events.entity.ReservationStatus;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.StatCard;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.OrganizerLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route(value = "organizer/event/:eventID/reservations", layout = OrganizerLayout.class)
@PageTitle("Gestion Réservations | FESTIVENT")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    private Event currentEvent;
    private Grid<Reservation> grid;
    private GridListDataView<Reservation> dataView;

    // Filtres
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    // KPIs (Cartes stats)
    private StatCard totalResaCard;
    private StatCard revenueCard;
    private StatCard seatsCard;

    public EventReservationsView(EventService eventService,
                                 ReservationService reservationService,
                                 UserService userService,
                                 SecurityService securityService,
                                 NavigationManager navigationManager) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        setPadding(true);
        setSpacing(true);
        setSizeFull();
        addClassName(LumoUtility.Background.BASE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> eventIdParam = event.getRouteParameters().get("eventID");
        if (eventIdParam.isPresent()) {
            try {
                Long eventId = Long.parseLong(eventIdParam.get());
                currentEvent = eventService.getEventById(eventId);

                // Sécurité : Vérifier appartenance
                User user = getCurrentUser();
                if (!currentEvent.getOrganisateur().getId().equals(user.getId()) && !user.isAdmin()) {
                    showError("Accès refusé.");
                    event.forwardTo("organizer/events");
                    return;
                }

                removeAll();
                buildUI();
                refreshData();

            } catch (Exception e) {
                showError("Événement introuvable.");
                event.forwardTo("organizer/events");
            }
        }
    }

    private void buildUI() {
        // 1. Header & Stats
        add(createHeader(), createKpiSection());

        // 2. Toolbar & Grid
        add(createToolbar(), createGrid());
    }

    private Component createHeader() {
        Button backBtn = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> navigationManager.goToMyEvents());

        H2 title = new H2("Réservations : " + currentEvent.getTitre());
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        HorizontalLayout header = new HorizontalLayout(backBtn, title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private Component createKpiSection() {
        FlexLayout layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);

        // Initialisation des cartes (valeurs mises à jour par refreshData)
        totalResaCard = new StatCard("Commandes", "0", VaadinIcon.FILE_TEXT, "Total", "#6366f1");
        revenueCard = new StatCard("Revenus", "0 DH", VaadinIcon.MONEY, "Confirmé", "#10b981");
        seatsCard = new StatCard("Places", "0", VaadinIcon.TICKET, "Sur " + currentEvent.getCapaciteMax(), "#f59e0b");

        styleCard(totalResaCard);
        styleCard(revenueCard);
        styleCard(seatsCard);

        layout.add(revenueCard, seatsCard, totalResaCard);
        return layout;
    }

    private void styleCard(StatCard card) {
        card.setMinWidth("200px");
        card.getStyle().set("flex", "1");
    }

    private Component createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher (Nom, Code)...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateFilter());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateFilter());

        Button exportBtn = new Button("Exporter CSV", new Icon(VaadinIcon.DOWNLOAD));
        exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Anchor downloadLink = new Anchor(getExportResource(), "");
        downloadLink.add(exportBtn);
        downloadLink.getElement().setAttribute("download", true);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter, downloadLink);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return toolbar;
    }

    private Component createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        // Grid Container
        VerticalLayout gridWrapper = new VerticalLayout(grid);
        gridWrapper.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Overflow.HIDDEN);
        gridWrapper.setPadding(false);
        gridWrapper.setSizeFull();

        grid.addColumn(Reservation::getCodeReservation).setHeader("Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getUtilisateur().getNomComplet()).setHeader("Client").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> DateFormatter.format(r.getDateReservation())).setHeader("Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(Reservation::getNombrePlaces).setHeader("Places").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(r -> PriceFormatter.format(r.getMontantTotal())).setHeader("Total").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(new ComponentRenderer<>(r -> new StatusBadge(r.getStatut().getLabel(), r.getStatut().getColor()))).setHeader("Statut");
        grid.addComponentColumn(this::createActions).setHeader("Actions");

        return gridWrapper;
    }

    private Component createActions(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();

        if (reservation.getStatut() == ReservationStatus.EN_ATTENTE) {
            Button confirmBtn = new Button(new Icon(VaadinIcon.CHECK));
            confirmBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            confirmBtn.setTooltipText("Confirmer");
            confirmBtn.addClickListener(e -> handleConfirm(reservation));
            actions.add(confirmBtn);
        }

        if (reservation.getStatut() != ReservationStatus.ANNULEE) {
            Button cancelBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            cancelBtn.setTooltipText("Annuler");
            cancelBtn.addClickListener(e -> handleCancel(reservation));
            actions.add(cancelBtn);
        }

        return actions;
    }

    // --- DATA & LOGIQUE ---

    private void refreshData() {
        // Liste
        List<Reservation> reservations = reservationService.findEventReservations(currentEvent.getId());
        dataView = grid.setItems(reservations);
        updateFilter();

        // Stats
        Map<String, Object> stats = reservationService.getReservationStatisticsByEvent(currentEvent.getId());

        revenueCard.setValue(PriceFormatter.format(safeGetDouble(stats, "totalRevenue")));
        seatsCard.setValue(String.valueOf(safeGetInt(stats, "totalPlaces")));
        totalResaCard.setValue(String.valueOf(safeGetLong(stats, "totalReservations")));

        long annul = safeGetLong(stats, "reservationsAnnulees");
        totalResaCard.setSubtitle(annul > 0 ? annul + " annulées" : "");
    }

    private void updateFilter() {
        if (dataView == null) return;
        dataView.setFilter(r -> {
            String search = searchField.getValue().trim().toLowerCase();
            boolean matchSearch = search.isEmpty() ||
                    r.getCodeReservation().toLowerCase().contains(search) ||
                    r.getUtilisateur().getNomComplet().toLowerCase().contains(search);
            boolean matchStatus = statusFilter.getValue() == null || r.getStatut() == statusFilter.getValue();
            return matchSearch && matchStatus;
        });
    }

    private StreamResource getExportResource() {
        return new StreamResource("reservations.csv", () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Code;Client;Date;Places;Montant;Statut\n");
            List<Reservation> items = reservationService.findEventReservations(currentEvent.getId());
            for (Reservation r : items) {
                sb.append(r.getCodeReservation()).append(";")
                        .append(r.getUtilisateur().getNomComplet()).append(";")
                        .append(DateFormatter.format(r.getDateReservation())).append(";")
                        .append(r.getNombrePlaces()).append(";")
                        .append(r.getMontantTotal()).append(";")
                        .append(r.getStatut().name()).append("\n");
            }
            return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        });
    }

    private void handleConfirm(Reservation r) {
        ConfirmDialogUtil.show("Confirmer ?", "Valider cette réservation ?", () -> {
            try {
                reservationService.confirmReservation(r.getId(), this.getCurrentUser().getId());
                showSuccess("Réservation confirmée.");
                refreshData();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private void handleCancel(Reservation r) {
        ConfirmDialogUtil.show("Annuler ?", "Action irréversible.", () -> {
            try {
                reservationService.annulerReservation(r.getId(), getCurrentUser().getId());
                showSuccess("Réservation annulée.");
                refreshData();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private User getCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        return userService.getUserByEmail(userDetails.getUsername());
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // Helpers Stats
    private Long safeGetLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return (val instanceof Number) ? ((Number) val).longValue() : 0L;
    }
    private Double safeGetDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
    }
    private Integer safeGetInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return (val instanceof Number) ? ((Number) val).intValue() : 0;
    }
}