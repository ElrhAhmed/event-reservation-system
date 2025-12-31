package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.StatCard;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.layout.OrganizerLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.List;
import java.util.Map;

@Route(value = "organizer/dashboard", layout = OrganizerLayout.class)
@PageTitle("Tableau de bord Organisateur | FESTIVENT")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    public OrganizerDashboardView(EventService eventService,
                                  UserService userService,
                                  SecurityService securityService,
                                  NavigationManager navigationManager) {
        this.eventService = eventService;
        this.userService = userService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        // 1. Organisateur
        User organizer = getCurrentOrganizer();
        if (organizer == null) {
            add(new Span("Erreur : Utilisateur non identifié."));
            return;
        }

        // 2. Données
        Map<String, Object> stats = eventService.getOrganizerStatistics(organizer.getId());
        List<Event> recentEvents = eventService.getEventsByOrganisateur(organizer.getId());

        // 3. UI
        add(
                createHeader(organizer),
                createKpiSection(stats),
                createRecentEventsSection(recentEvents)
        );
    }

    private User getCurrentOrganizer() {
        var userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            return userService.getUserByEmail(userDetails.getUsername());
        }
        return null;
    }

    private HorizontalLayout createHeader(User organizer) {
        VerticalLayout text = new VerticalLayout();
        text.setPadding(false);
        text.setSpacing(false);

        H2 title = new H2("Vue d'ensemble");
        title.addClassName(LumoUtility.Margin.Bottom.NONE);

        Span subtitle = new Span("Bienvenue, " + organizer.getPrenom() + ". Voici les performances de vos événements.");
        subtitle.addClassName(LumoUtility.TextColor.SECONDARY);

        text.add(title, subtitle);

        Button createBtn = new Button("Nouvel Événement", new Icon(VaadinIcon.PLUS));
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(e -> navigationManager.goToCreateEvent());

        HorizontalLayout header = new HorizontalLayout(text, createBtn);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        return header;
    }

    private FlexLayout createKpiSection(Map<String, Object> stats) {
        FlexLayout layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);
        layout.addClassName(LumoUtility.Margin.Bottom.LARGE);

        // KPI 1 : Revenus (Vert)
        Double revenue = safeGetDouble(stats, "totalRevenue");
        StatCard revenueCard = new StatCard(
                "Revenu Total",
                PriceFormatter.format(revenue),
                VaadinIcon.MONEY,
                "Chiffre d'affaires généré",
                "#10b981" // Vert Émeraude
        );
        styleCard(revenueCard);

        // KPI 2 : Réservations (Bleu Indigo)
        long totalReservations = safeGetLong(stats, "totalReservations");
        StatCard salesCard = new StatCard(
                "Réservations",
                String.valueOf(totalReservations),
                VaadinIcon.TICKET,
                "Billets vendus au total",
                "#6366f1" // Indigo
        );
        styleCard(salesCard);

        // KPI 3 : Événements Publiés (Violet)
        long published = safeGetLong(stats, "publishedEvents");
        long totalEvents = safeGetLong(stats, "totalEvents");
        StatCard eventsCard = new StatCard(
                "Événements Actifs",
                String.valueOf(published),
                VaadinIcon.CALENDAR_CLOCK,
                "Sur " + totalEvents + " événements créés",
                "#8b5cf6" // Violet
        );
        styleCard(eventsCard);

        // KPI 4 : Brouillons (Orange/Gris)
        long drafts = safeGetLong(stats, "draftEvents");
        StatCard draftCard = new StatCard(
                "Brouillons",
                String.valueOf(drafts),
                VaadinIcon.EDIT,
                "En attente de publication",
                drafts > 0 ? "#f59e0b" : "#9ca3af" // Orange si >0, sinon Gris
        );
        styleCard(draftCard);

        layout.add(revenueCard, salesCard, eventsCard, draftCard);
        return layout;
    }

    private void styleCard(StatCard card) {
        card.setMinWidth("240px");
        card.getStyle().set("flex", "1");
    }

    private VerticalLayout createRecentEventsSection(List<Event> events) {
        H3 title = new H3("Événements Récents");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.SMALL);

        // Grid moderne avec fond blanc et ombre
        VerticalLayout gridContainer = new VerticalLayout();
        gridContainer.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.SMALL);

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.addClassName(LumoUtility.Border.NONE);

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(new ComponentRenderer<>(event -> {
            return new StatusBadge(event.getStatut().getLabel(), event.getStatut().getColor());
        })).setHeader("Statut").setAutoWidth(true);

        grid.addColumn(e -> DateFormatter.format(e.getDateDebut()))
                .setHeader("Date");

        grid.addColumn(e -> e.getVille())
                .setHeader("Ville");

        grid.addColumn(e -> e.getCapaciteMax() + " places")
                .setHeader("Capacité")
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        // Tri local
        events.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
        grid.setItems(events);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(true);
        gridContainer.add(grid);

        if (events.isEmpty()) {
            VerticalLayout empty = new VerticalLayout();
            empty.setAlignItems(Alignment.CENTER);
            empty.add(new Span("Aucun événement créé pour le moment."),
                    new Button("Créer mon premier événement", e -> navigationManager.goToCreateEvent()));
            return new VerticalLayout(title, empty);
        }

        return new VerticalLayout(title, gridContainer);
    }

    // Helpers safeGet...
    private Long safeGetLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }

    private Double safeGetDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}