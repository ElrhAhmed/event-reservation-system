package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.*;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.OrganizerLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.ArrayList;
import java.util.List;

@Route(value = "organizer/reservations", layout = OrganizerLayout.class)
@PageTitle("Toutes les Réservations | FESTIVENT")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class OrganizerAllReservationsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    private Grid<Reservation> grid;
    private GridListDataView<Reservation> dataView;

    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;
    // Nouveaux filtres
    private ComboBox<Event> eventFilter;
    private ComboBox<EventCategory> categoryFilter;

    // Liste des événements de l'organisateur (pour le filtre)
    private List<Event> organizerEvents;

    public OrganizerAllReservationsView(EventService eventService,
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

        add(createHeader(), createToolbar(), createGrid());

        refreshData();
    }

    private Component createHeader() {
        H2 title = new H2("Toutes les Réservations");
        title.addClassName(LumoUtility.Margin.NONE);
        return new HorizontalLayout(title);
    }

    private Component createToolbar() {
        // 1. Recherche Textuelle
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher (Client, Code)...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("250px");
        searchField.addValueChangeListener(e -> updateFilter());

        // 2. Filtre Statut
        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> updateFilter());

        // 3. Filtre Événement (Rempli dynamiquement plus tard)
        eventFilter = new ComboBox<>();
        eventFilter.setPlaceholder("Filtrer par Événement");
        eventFilter.setItemLabelGenerator(Event::getTitre);
        eventFilter.setClearButtonVisible(true);
        eventFilter.setWidth("250px");
        eventFilter.addValueChangeListener(e -> updateFilter());

        // 4. Filtre Catégorie
        categoryFilter = new ComboBox<>();
        categoryFilter.setPlaceholder("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(EventCategory::getLabel);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("180px");
        categoryFilter.addValueChangeListener(e -> updateFilter());

        // Layout flexible (Wrap) pour s'adapter aux petits écrans
        FlexLayout toolbar = new FlexLayout(searchField, statusFilter, eventFilter, categoryFilter);
        toolbar.setWidthFull();
        toolbar.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        toolbar.addClassName(LumoUtility.Gap.MEDIUM);

        return toolbar;
    }

    private Component createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(Reservation::getCodeReservation).setHeader("Code").setAutoWidth(true).setSortable(true);

        grid.addColumn(r -> r.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(r -> r.getEvenement().getCategorie().getLabel())
                .setHeader("Catégorie")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(r -> r.getUtilisateur().getNomComplet()).setHeader("Client").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> DateFormatter.format(r.getDateReservation())).setHeader("Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getNombrePlaces() + " pl.").setHeader("Qté").setAutoWidth(true);
        grid.addColumn(r -> PriceFormatter.format(r.getMontantTotal())).setHeader("Total").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(r ->
                new StatusBadge(r.getStatut().getLabel(), r.getStatut().getColor())
        )).setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions");

        return grid;
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

    private void refreshData() {
        User organizer = getCurrentUser();
        if (organizer == null) return;

        // 1. Récupérer les événements de l'organisateur
        organizerEvents = eventService.getEventsByOrganisateur(organizer.getId());

        // Mettre à jour le filtre avec ces événements
        eventFilter.setItems(organizerEvents);

        List<Reservation> allReservations = new ArrayList<>();

        // 2. Récupérer les réservations pour ces événements
        for (Event event : organizerEvents) {
            allReservations.addAll(reservationService.findEventReservations(event.getId()));
        }

        // 3. Trier
        allReservations.sort((r1, r2) -> r2.getId().compareTo(r1.getId()));

        dataView = grid.setItems(allReservations);
    }

    private void updateFilter() {
        if (dataView == null) return;
        dataView.setFilter(r -> {
            String search = searchField.getValue().trim().toLowerCase();
            boolean matchSearch = search.isEmpty()
                    || r.getCodeReservation().toLowerCase().contains(search)
                    || r.getUtilisateur().getNomComplet().toLowerCase().contains(search)
                    || r.getEvenement().getTitre().toLowerCase().contains(search);

            boolean matchStatus = statusFilter.getValue() == null
                    || r.getStatut() == statusFilter.getValue();

            // NOUVEAU : Filtre par Événement
            boolean matchEvent = eventFilter.getValue() == null
                    || r.getEvenement().getId().equals(eventFilter.getValue().getId());

            // NOUVEAU : Filtre par Catégorie
            boolean matchCategory = categoryFilter.getValue() == null
                    || r.getEvenement().getCategorie() == categoryFilter.getValue();

            return matchSearch && matchStatus && matchEvent && matchCategory;
        });
    }

    private void handleConfirm(Reservation r) {
        ConfirmDialogUtil.show("Confirmer ?", "Valider cette réservation ?", () -> {
            try {
                reservationService.confirmReservation(r.getId(), this.getCurrentUser().getId());
                Notification.show("Confirmé").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshData();
            } catch (Exception e) { Notification.show(e.getMessage()); }
        });
    }

    private void handleCancel(Reservation r) {
        ConfirmDialogUtil.show("Annuler ?", "Action irréversible.", () -> {
            try {
                // Pour l'organisateur, on peut appeler une méthode d'annulation administrative si besoin
                // Ici on utilise la même méthode (qui a la règle des 48h, attention)
                // Si l'organisateur doit pouvoir annuler n'importe quand, il faudra une méthode spécifique dans le service.
                reservationService.annulerReservation(r.getId(), r.getUtilisateur().getId()); // Simule action utilisateur pour l'instant
                Notification.show("Annulé").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshData();
            } catch (Exception e) {
                // En tant qu'organisateur, on voudrait peut-être outrepasser la règle des 48h ?
                // Pour l'instant on affiche l'erreur standard.
                Notification.show("Erreur : " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private User getCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        return (userDetails != null) ? userService.getUserByEmail(userDetails.getUsername()) : null;
    }
}