package ma.projet.events.ui.view.admin;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Reservation;
import ma.projet.events.entity.ReservationStatus;
import ma.projet.events.service.ReservationService;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.Comparator;
import java.util.List;

@Route(value = "admin/reservations", layout = AdminLayout.class)
@PageTitle("Toutes les Réservations | FESTIVENT Admin")
@RolesAllowed("ADMIN")
public class AdminReservationsView extends VerticalLayout {

    private final ReservationService reservationService;

    private Grid<Reservation> grid;
    private GridListDataView<Reservation> dataView;
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    public AdminReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        add(createHeader(), createToolbar());

        // Grid Wrapper
        VerticalLayout gridWrapper = new VerticalLayout();
        gridWrapper.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Overflow.HIDDEN);
        gridWrapper.setPadding(false);
        gridWrapper.setSizeFull();

        grid = createGrid();
        gridWrapper.add(grid);

        add(gridWrapper);

        refreshData();
    }

    private Component createHeader() {
        H2 title = new H2("Supervision Réservations");
        title.addClassName(LumoUtility.Margin.NONE);
        return title;
    }

    private Component createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher (Code, Client, Event)...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addClassName(LumoUtility.Flex.GROW);
        searchField.addValueChangeListener(e -> updateFilter());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> updateFilter());

        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH), e -> refreshData());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter, refreshBtn);
        toolbar.setWidthFull();
        toolbar.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return toolbar;
    }

    private Grid<Reservation> createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(Reservation::getCodeReservation).setHeader("Code").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getEvenement().getTitre()).setHeader("Événement").setAutoWidth(true).setSortable(true).setFlexGrow(1);
        grid.addColumn(r -> r.getUtilisateur().getNomComplet()).setHeader("Client").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getEvenement().getOrganisateur().getNomComplet()).setHeader("Organisateur").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> DateFormatter.format(r.getDateReservation())).setHeader("Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> PriceFormatter.format(r.getMontantTotal())).setHeader("Montant").setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.addColumn(new ComponentRenderer<>(r -> new StatusBadge(r.getStatut().getLabel(), r.getStatut().getColor()))).setHeader("Statut").setAutoWidth(true);

        return grid;
    }

    private void refreshData() {
        List<Reservation> allReservations = reservationService.getReservationsWithFilters(null, null, null);
        allReservations.sort(Comparator.comparing(Reservation::getId).reversed());
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

            return matchSearch && matchStatus;
        });
    }
}