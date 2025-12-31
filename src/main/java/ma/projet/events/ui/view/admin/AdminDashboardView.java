package ma.projet.events.ui.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.Role;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.StatCard;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.List;
import java.util.Map;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@PageTitle("Dashboard Admin | FESTIVENT")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final SecurityService securityService;

    public AdminDashboardView(UserService userService,
                              EventService eventService,
                              ReservationService reservationService,
                              SecurityService securityService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.securityService = securityService;

        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        // 1. En-tête
        add(createHeader());

        // 2. KPIs Financiers
        add(createSectionTitle("Performance Commerciale"));
        add(createBusinessStats());

        // 3. KPIs Utilisateurs
        add(createSectionTitle("Communauté"));
        add(createUserStats());

        // 4. KPIs Événements
        add(createSectionTitle("Catalogue Événements"));
        add(createEventStats());
    }

    private Component createHeader() {
        var userDetails = securityService.getAuthenticatedUser();
        String username = userDetails != null ? userDetails.getUsername() : "Admin";

        H2 title = new H2("Vue d'ensemble");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Span subtitle = new Span("Bienvenue, " + username + ". Voici l'état de santé de la plateforme.");
        subtitle.addClassName(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.addClassName(LumoUtility.Margin.Bottom.LARGE);
        return header;
    }

    private Component createSectionTitle(String title) {
        H3 h3 = new H3(title);
        h3.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.HEADER, LumoUtility.Margin.Top.LARGE, LumoUtility.Margin.Bottom.SMALL);
        return h3;
    }

    // --- KPIs BUSINESS ---
    private Component createBusinessStats() {
        Map<String, Object> stats = reservationService.getReservationStatistics();
        FlexLayout layout = createCardLayout();

        StatCard revenueCard = new StatCard("Volume d'Affaires", PriceFormatter.format((Double) stats.get("totalRevenue")), VaadinIcon.MONEY, "Total généré", "#10b981");
        styleCard(revenueCard);

        StatCard totalResaCard = new StatCard("Réservations Totales", stats.get("totalReservations").toString(), VaadinIcon.TICKET, stats.get("confirmedReservations") + " confirmées", "#6366f1");
        styleCard(totalResaCard);

        long total = (Integer) stats.get("totalReservations");
        long cancelled = (Long) stats.get("cancelledReservations");
        double rate = total > 0 ? ((double) cancelled / total) * 100 : 0;

        StatCard cancelCard = new StatCard("Taux d'Annulation", String.format("%.1f %%", rate), VaadinIcon.BAN, cancelled + " annulées", "#ef4444");
        styleCard(cancelCard);

        layout.add(revenueCard, totalResaCard, cancelCard);
        return layout;
    }

    // --- KPIs USERS ---
    private Component createUserStats() {
        FlexLayout layout = createCardLayout();

        long totalUsers = userService.countAllUsers();
        StatCard totalCard = new StatCard("Utilisateurs Inscrits", String.valueOf(totalUsers), VaadinIcon.USERS, "Comptes actifs", "#3b82f6");
        styleCard(totalCard);

        long clients = userService.countUsersByRole(Role.CLIENT);
        StatCard clientCard = new StatCard("Clients", String.valueOf(clients), VaadinIcon.USER, "Acheteurs", "#8b5cf6");
        styleCard(clientCard);

        long organizers = userService.countUsersByRole(Role.ORGANIZER);
        StatCard orgCard = new StatCard("Organisateurs", String.valueOf(organizers), VaadinIcon.BRIEFCASE, "Créateurs", "#f59e0b");
        styleCard(orgCard);

        layout.add(totalCard, clientCard, orgCard);
        return layout;
    }

    // --- KPIs EVENTS ---
    private Component createEventStats() {
        List<Event> allEvents = eventService.getAllEvents();
        long published = allEvents.stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long drafts = allEvents.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();
        long finished = allEvents.stream().filter(e -> e.getStatut() == EventStatus.TERMINE).count();

        FlexLayout layout = createCardLayout();

        StatCard pubCard = new StatCard("Événements Publiés", String.valueOf(published), VaadinIcon.CALENDAR_CLOCK, "Visibles en ligne", "#10b981");
        styleCard(pubCard);

        StatCard draftCard = new StatCard("Brouillons", String.valueOf(drafts), VaadinIcon.EDIT, "En attente", "#9ca3af");
        styleCard(draftCard);

        StatCard finishedCard = new StatCard("Terminés / Passés", String.valueOf(finished), VaadinIcon.ARCHIVE, "Historique", "#6b7280");
        styleCard(finishedCard);

        layout.add(pubCard, draftCard, finishedCard);
        return layout;
    }

    private FlexLayout createCardLayout() {
        FlexLayout layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);
        return layout;
    }

    private void styleCard(StatCard card) {
        card.setMinWidth("250px");
        card.getStyle().set("flex", "1");
    }
}